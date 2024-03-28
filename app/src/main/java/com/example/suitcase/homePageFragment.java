package com.example.suitcase;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.suitcase.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class homePageFragment extends Fragment {
    private RecyclerView recyclerView;
    private GridAdapter adapter;
    private List<Item> itemList = new ArrayList<>();
    private String itemId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        recyclerView = view.findViewById(R.id.items_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter = new GridAdapter();
        recyclerView.setAdapter(adapter);

        // Add swipe gesture functionality
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe left (delete)
                    Item removedItem = itemList.remove(position); // Remove from local list
                    adapter.notifyItemRemoved(position);
                    removeItemFromDatabase(removedItem.getId()); // Remove from Firebase by ID
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe right (mark as purchased)
                    Item item = itemList.get(position);
                    // Mark item as purchased
                    item.setPurchased(true);
                    // Update UI
                    adapter.notifyItemChanged(position);
                    // Update Firebase
                    updateItemInDatabase(item);
                }
            }
            // Method to remove item from Firebase database by ID
            private void removeItemFromDatabase(String itemId) {
                DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("items").child(itemId);
                itemsRef.removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Item removed successfully from Firebase
                                Toast.makeText(getContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                                Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Error deleting item: " + e.getMessage());
                            }
                        });
            }

            // Method to update item in Firebase database
            private void updateItemInDatabase(Item item) {
                DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("items").child(item.getId());
                itemsRef.setValue(item)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Item updated successfully in Firebase
                                Toast.makeText(getContext(), "Item marked as purchased", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                                Toast.makeText(getContext(), "Failed to mark item as purchased", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Error updating item: " + e.getMessage());
                            }
                        });
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Drawable deleteIcon = getResources().getDrawable(R.drawable.ic_delete); // Your delete icon
                Drawable tickIcon = getResources().getDrawable(R.drawable.ic_check); // Your tick icon

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (dX > 0) {
                        // Swipe right (mark as purchased)
                        // Draw green background
                        ColorDrawable background = new ColorDrawable(Color.GREEN);
                        background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                        background.draw(c);

                        // Draw tick icon
                        int iconMargin = (itemView.getHeight() - tickIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + (itemView.getHeight() - tickIcon.getIntrinsicHeight()) / 2;
                        int iconBottom = iconTop + tickIcon.getIntrinsicHeight();
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = itemView.getLeft() + iconMargin + tickIcon.getIntrinsicWidth();
                        tickIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        tickIcon.draw(c);
                    } else {
                        // Swipe left (delete)
                        // Draw red background
                        ColorDrawable background = new ColorDrawable(Color.RED);
                        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        background.draw(c);

                        // Draw delete icon
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        // Read data from Firebase database
        FirebaseDatabase.getInstance().getReference("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear(); // Clear the list before populating it
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item item = snapshot.getValue(Item.class);
                    item.setId(snapshot.getKey()); // Set the ID
                    itemList.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        return view;
    }

    // GridAdapter inner class
    private class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView itemName;
            private final TextView itemPrice;
            private final ImageView itemImage;

            ViewHolder(View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.item_name);
                itemPrice = itemView.findViewById(R.id.item_price);
                itemImage = itemView.findViewById(R.id.item_image);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull GridAdapter.ViewHolder holder, int position) {
            Item item = itemList.get(position);
            holder.itemName.setText(item.getName());
            holder.itemPrice.setText(item.getPrice());
            Picasso.get().load(item.getImageUrl()).into(holder.itemImage);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EditActivity.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("name", item.getName());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("price", item.getPrice());
                intent.putExtra("purchased", item.isPurchased());
                intent.putExtra("imageUrl", item.getImageUrl());
                intent.putExtra("lat", item.getLatTag());
                intent.putExtra("lon", item.getLonTag());
                startActivity(intent);
            });
        }

        @NonNull
        @Override
        public GridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }
    }
}

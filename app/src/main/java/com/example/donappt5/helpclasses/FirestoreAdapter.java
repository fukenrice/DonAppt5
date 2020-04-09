package com.example.donappt5.helpclasses;

import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.recyclerview.widget.RecyclerView;

public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements EventListener<QuerySnapshot> {

    // ...

    @Override
    public void onEvent(QuerySnapshot documentSnapshots,
                        FirebaseFirestoreException e) {

        // Handle errors
        if (e != null) {
            Log.w("FirestoreadapterLogs", "onEvent:error", e);
            return;
        }

        // Dispatch the event
        for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
            // Snapshot of the changed document
            DocumentSnapshot snapshot = change.getDocument();

            switch (change.getType()) {
                case ADDED:
                    // TODO: handle document added
                    break;
                case MODIFIED:
                    // TODO: handle document modified
                    break;
                case REMOVED:
                    // TODO: handle document removed
                    break;
            }
        }

        //onDataChanged(); //TODO implement realtime changes (lowest priority)
    }

    // ...
}
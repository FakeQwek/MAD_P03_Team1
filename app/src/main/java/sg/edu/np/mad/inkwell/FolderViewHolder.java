package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class FolderViewHolder extends RecyclerView.ViewHolder {
    // FolderViewHolder for recycler view
    Button folderButton;

    RecyclerView recyclerView;

    ConstraintLayout constraintLayout;

    public FolderViewHolder(View view) {
        super(view);

        folderButton = view.findViewById(R.id.folderButton);

        recyclerView = view.findViewById(R.id.recyclerView);

        constraintLayout = view.findViewById(R.id.constraintLayout);
    }
}

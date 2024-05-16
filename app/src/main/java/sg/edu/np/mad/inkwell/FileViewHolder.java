package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

public class FileViewHolder extends RecyclerView.ViewHolder {
    Button fileButton;

    public FileViewHolder(View view) {
        super(view);

        fileButton = view.findViewById(R.id.fileButton);
    }
}

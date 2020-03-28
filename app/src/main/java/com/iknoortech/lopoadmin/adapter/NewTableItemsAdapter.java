package com.iknoortech.lopoadmin.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iknoortech.lopoadmin.R;
import com.iknoortech.lopoadmin.model.newTable.NewTable;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewTableItemsAdapter extends RecyclerView.Adapter<NewTableItemsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NewTable> arrayList;

    public NewTableItemsAdapter(Context context, ArrayList<NewTable> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_new_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvCount.setText(String.valueOf(position + 1));
        holder.edtName.setText(arrayList.get(position).getColumnName());
        holder.edtValue.setText(arrayList.get(position).getColumnValue());

        holder.edtValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayList.get(position).setColumnValue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayList.get(position).setColumnName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayList.size() == 1) {
                    arrayList.get(position).setColumnName("");
                    arrayList.get(position).setColumnValue("");
                } else {
                    arrayList.remove(position);
                }

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private EditText edtName, edtValue;
        private TextView tvCount;
        private Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            edtName = itemView.findViewById(R.id.editText3);
            edtValue = itemView.findViewById(R.id.editText4);
            tvCount = itemView.findViewById(R.id.textView6);
            btnDelete = itemView.findViewById(R.id.button3);
        }
    }
}

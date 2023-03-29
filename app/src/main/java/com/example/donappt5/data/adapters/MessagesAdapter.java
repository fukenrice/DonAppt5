package com.example.donappt5.data.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donappt5.R;
import com.example.donappt5.data.model.ForumMessage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessagesAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<ForumMessage> objects;
    ImageView ivcomm;


    public MessagesAdapter(Context context, ArrayList<ForumMessage> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_message, parent, false);
        }

        ForumMessage c = getMessage(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvName)).setText(c.username);
        ((TextView) view.findViewById(R.id.tvMessage)).setText(String.valueOf(c.comment));
        ((TextView) view.findViewById(R.id.tvDatetime)).setText(String.valueOf(c.datetime));
        ivcomm = view.findViewById(R.id.ivCommentator);
        //((ImageView)(view.findViewById(R.id.ivCommentator))).setImageResource(R.drawable.ic_launcher_foreground);

        //ImageView ivinad = view.findViewById(R.id.ivImage);
        Picasso.with(ctx).setLoggingEnabled(true);
        if (c.profileurl != null) {
            Log.d("photourlmessages", "got " + c.profileurl);
            //Picasso.get().load(c.pro).into(ivinHeader);
            Picasso.with(ctx).load(c.profileurl).fit().into(ivcomm);
        }
        return view;
    }

    ForumMessage getMessage(int position) {
        return ((ForumMessage) getItem(position));
    }
}

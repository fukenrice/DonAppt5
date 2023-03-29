package com.example.donappt5.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donappt5.R;
import com.example.donappt5.data.model.Friend;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FriendsAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<Friend> objects;

    public FriendsAdapter(Context context, ArrayList<Friend> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        if (objects == null) return 0;
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
            view = lInflater.inflate(R.layout.item_friend, parent, false);
        }

        Friend f = getFriend(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvName)).setText(f.name);
        ((TextView) view.findViewById(R.id.tvChars)).setText("charsnotloaded");
        ((ImageView)(view.findViewById(R.id.ivFriend))).setImageResource(R.drawable.ic_launcher_foreground);

        //ImageView ivinad = view.findViewById(R.id.ivImage);

        if (!f.photourl.isEmpty()) {
            //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
            Picasso.with(ctx).load(f.photourl).fit().into((ImageView)(view.findViewById(R.id.ivFriend)));
        }
        if (f.chars != null) {
            if (f.chars.size() != 0) {
                String hs;
                if (f.chars.size() == 1) {
                    hs = "Creator of " + f.chars.get(0);
                    ((TextView) view.findViewById(R.id.tvChars)).setText(hs);
                }
                else if (f.chars.size() == 2) {
                    hs = "Creator of " + f.chars.get(0) + " and " + f.chars.get(1);
                    ((TextView) view.findViewById(R.id.tvChars)).setText(hs);
                }
                else {
                    hs = "Creator of " + f.chars.get(0) + ", " + f.chars.get(1) + " and " + String.valueOf(f.chars.size() - 2) + " more";
                    ((TextView) view.findViewById(R.id.tvChars)).setText(hs);
                }
            } else {
                String hs = "";
                ((TextView) view.findViewById(R.id.tvChars)).setText(hs);
            }
            String hs = "";
            ((TextView) view.findViewById(R.id.tvChars)).setText(hs);
        }
        return view;
    }

    public Friend getFriend(int position) {
        return ((Friend) getItem(position));
    }
}
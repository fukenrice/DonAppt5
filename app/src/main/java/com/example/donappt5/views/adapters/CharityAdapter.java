package com.example.donappt5.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donappt5.R;
import com.example.donappt5.data.model.Charity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CharityAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    public ArrayList<Charity> objects;

    public CharityAdapter(Context context, ArrayList<Charity> products) {
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
            view = lInflater.inflate(R.layout.item_charity, parent, false);
        }

        Charity c = getCharity(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvDescr)).setText(c.briefDescription);
        ((TextView) view.findViewById(R.id.tvName)).setText(c.name);
        ((ImageView)(view.findViewById(R.id.ivImage))).setImageResource(R.drawable.ic_launcher_foreground);

        //ImageView ivinad = view.findViewById(R.id.ivImage);

        if (!c.photourl.isEmpty()) {
            //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
            Picasso.with(ctx).load(c.photourl).fit().into((ImageView)(view.findViewById(R.id.ivImage)));
        }
        return view;
    }

    public Charity getCharity(int position) {
        return ((Charity) getItem(position));
    }

    public void addData(List<Charity> list) {
        objects.addAll(list);
    }

    public void clear() {
        objects.clear();
    }
}
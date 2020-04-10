package com.example.donappt5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donappt5.helpclasses.Charity;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;

public class CharityAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Charity> objects;

    CharityAdapter(Context context, ArrayList<Charity> products) {
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
            view = lInflater.inflate(R.layout.activity_chlistel, parent, false);
        }

        Charity c = getCharity(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvDescr)).setText(c.briefDescription);
        ((TextView) view.findViewById(R.id.tvRating)).setText(String.valueOf(c.trust));
        ((TextView) view.findViewById(R.id.tvName)).setText(c.name);
        ImageView ivinad = view.findViewById(R.id.ivImage);

        if (c.photourl != null) {
            //Picasso.get().load(user.getPhotoUrl()).into(ivinHeader);
            Picasso.with(ctx).load(c.photourl).fit().into(ivinad);
        }
        // присваиваем чекбоксу обработчик
        // заполняем данными из товаров: в корзине или нет
        return view;
    }

    // товар по позиции
    Charity getCharity(int position) {
        return ((Charity) getItem(position));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
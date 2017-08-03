package com.zhuang.likeview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuang.likeviewlibrary.LikeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new MyAdapter());
    }

    private List<News> createData() {
        Random random = new Random(47);
        List<News> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            News news = new News();
            news.setTitle(i + "我是标题");
            news.setContent("我是内容我是内容");
            news.setLikeCount(random.nextInt(1000));
            news.setHasLike(i % 4 == 0);
            list.add(news);
        }
        return list;
    }

    class MyAdapter extends BaseAdapter {

        List<News> list;

        public MyAdapter() {
            list = createData();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item_like, null);
            }

            LikeView likeView = (LikeView) convertView.findViewById(R.id.likeView);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView content = (TextView) convertView.findViewById(R.id.content);

            final News news = list.get(position);
            title.setText(news.getTitle());
            content.setText(news.getContent());
            likeView.setHasLike(news.isHasLike());
            likeView.setText(news.getLikeCount() + "");

            likeView.setOnLikeListeners(new LikeView.OnLikeListeners() {
                @Override
                public void like(boolean isCancel) {
                    news.setHasLike(!isCancel);
                    news.addLikeCount();
                }
            });
            return convertView;
        }
    }

}

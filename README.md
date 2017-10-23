# LikeView

一个仿即刻点赞效果的view，同时可以设置图标飞出父view。例如listview中使用，可以飞出当前itemview。

![](https://github.com/likeadog/LikeView/blob/master/screenshot/1.gif)
![](https://github.com/likeadog/LikeView/blob/master/screenshot/2.gif)
![](https://github.com/likeadog/LikeView/blob/master/screenshot/3.gif)

## gradle

    compile 'com.zhuang:likeview:2.1'

## xml
    <com.zhuang.likeviewlibrary.LikeView
    android:id="@+id/likeView"  
    android:layout_width="wrap_content"  
    android:layout_height="wrap_content"  
    app:like_canCancel="true" //是否可以取消点赞，设置了该属性之后，第一次点击新增点赞，第二次点击取消点赞。  
    app:like_iconSize="18dp" //点赞的图标的大小，也就是那个大拇指的大小。    
    app:like_hasFly="false" //是否有图标飞出  
    app:like_textSize="12sp" />//文字大小，也就是点赞数的文字大小。  

  
注意：这里的layout_width，layout_height实际上并无效果，likeview的大小由其icon与text大小决定。

## likeview的点击事件监听

    likeView.setOnLikeListeners(new LikeView.OnLikeListeners() {
        @Override
       public void like(boolean isCancel) {
            //isCancel
           //如果设置了like_canCancel为false，则isCancel可以不管，此时表示likeview被点击了
           //如果设置了like_canCancel为true,表示可以取消点赞，那么isCancel为true时，表示取消点赞，为false时，表示点赞
       }
    });







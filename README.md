# VisibleSensorLayout
一个提供的可见性感应的android视图容器

## 说明
1、适用于各种需要view可见性监测的业务场景，譬如播放器自动启播和暂停，数据上报等等。 

2、多层嵌套的ViewPager和RecyclerView等滚动容器也适用。

## 引入
1、项目添加jitpack仓库地址:
```
maven { url 'https://jitpack.io' }
```

2、添加依赖: 
```
implementation 'com.github.iwtsus:VisibleSensorLayout:1.0.0'
```

## 使用
```
visibleSensorLayout.basicRectView =  //基于判断可见性范围的view，不设置即整个屏幕
visibleSensorLayout.visibilityListener = object :VisibleSensorLayout.VisibilityListener{
    override fun onVisibleStateChange(state: VisibleSensorLayout.State) {
        when(state){
            VisibleSensorLayout.State.COMPLETELY_VISIBLE->{
                //全部可见
            }
            VisibleSensorLayout.State.PARTIALLY_VISIBLE->{
                //部分可见
            }
            VisibleSensorLayout.State.GONE->{
                //不可见
            }
        }
    }
}





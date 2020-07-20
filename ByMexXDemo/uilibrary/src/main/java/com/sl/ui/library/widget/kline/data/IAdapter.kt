package com.sl.ui.library.widget.kline.data

import android.database.DataSetObserver

/**
 * @Author: Bertking
 * @Date：2019/3/11-10:41 AM
 * @Description: 数据适配器
 */
interface IAdapter {
    /**
     * 获取点的数目
     *
     * @return
     */
    fun getCount(): Int

    /**
     * 通过序号获取item
     *
     * @param position 对应的序号
     * @return 数据实体
     */
    fun getItem(position: Int): Any

    /**
     * 通过序号获取时间
     *
     * @param position
     * @return
     */
    fun getDate(position: Int): String

    /**
     * 注册一个数据观察者
     *
     * @param observer 数据观察者
     */
    fun registerDataSetObserver(observer: DataSetObserver)

    /**
     * 移除一个数据观察者
     *
     * @param observer 数据观察者
     */
    fun unregisterDataSetObserver(observer: DataSetObserver)

    /**
     * 当数据发生变化时调用
     */
    fun notifyDataSetChanged()
}
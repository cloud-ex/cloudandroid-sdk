package com.sl.contract.library.data

enum class OrderPriceType {
    /**
     * 限价
     */
    CONTRACT_ORDER_LIMIT,
    /**
     * 市价
     */
    CONTRACT_ORDER_MARKET,
    /**
     * 计划
     */
    CONTRACT_ORDER_PLAN,
    /**
     * 买一价
     */
    CONTRACT_ORDER_BID_PRICE,
    /**
     * 卖一价
     */
    CONTRACT_ORDER_ASK_PRICE,
    /**
     * 限价(高级委托)
     */
    CONTRACT_ORDER_ADVANCED_LIMIT,
}
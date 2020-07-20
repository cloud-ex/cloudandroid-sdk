# cloudandroid-sdk
合约云安卓SDK
# 安卓合约云SDK接入文档 
### 1.更新历史
#### 2020-06-20 
```
1、初始化文档
```

### 2.前言和说明  
* 说明文档适用于所有与合作伙伴的安卓版客户端，在接入之前，请确认已跟我方运营同事获得HTTP和WS相关参数。如还未获取，请联系我方运营同事，  
* SDK采用AndroidX感知UI层组件生命周期，接入方需确保，Activity和Fragment采用AndroidX导包

### 3. 准备工作导入SDK包  

* APP的build.gradle中进行资源引用
```
    api "androidx.core:core-ktx:1.2.0"
    api "org.jetbrains.anko:anko:0.10.7"
    api 'androidx.lifecycle:lifecycle-extensions:2.0.0'

    //OkHttp
    api "com.squareup.okhttp3:okhttp:4.0.1"
    api "com.squareup.okio:okio:1.15.0"
    api files('libs\\fastjson-1.1.71.android.jar')
    //合约SDK
    api files('libs\\contract_sdkx_2.0_release.aar')
    
    #如APP本身存在该引用依赖，可不导入
```
  

* 配置AndroidManifest  
在工程AndroidManifest.xml中配置用户权限
请将下面权限配置代码复制到 AndroidManifest.xml 文件中 :
```
    <uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

* 混淆配置  
  SDK目录想相关文件已经过混淆，打包时，需做如下配置
```
    -dontwarn com.alibaba.fastjson.**
    -keep class com.alibaba.fastjson.** { *; }
    -keep class com.contract.sdk.** {*;}

```

### 4.SDK初始化(ContractSDKAgent类)
* BaseApplication里初始化sdk（如果APP有多进程，需注意避免SDK多次初始化） **（必接）**

```
    val contractHttpConfig = ContractHttpConfig()
    contractHttpConfig.prefixHeader = "ex"
    contractHttpConfig.contractUrl ="http://swapapi.nmghhzy.cn/"//设置HTTP接口请求域名
    contractHttpConfig.contractWsUrl="wss://swapws.hongyachina.com.cn/realTime"//设置合约ws
    //合约SDK Http配置初始化
    ContractSDKAgent.httpConfig = contractHttpConfig
    //是否是合约云SDK
    ContractSDKAgent.isContractCloudSDK = true
    //通知合约SDK语言环境 目前只支持中英文
    ContractSDKAgent.isZhEnv = true
    //合约SDK 必须设置 在最后调用
    ContractSDKAgent.init(this.application)
    //控制日志输出
    ContractSDKAgent.logEnabled = true
    
```

  
* 登录/退出登录

```
    //登录成功，通知SDK
    val user = ContractUser()
    user.token = "648dee29eca7034f29fc2d940382ee"
    user.expiredTs = "1765075396772000"
    user.accessKey = "3d2907dd-5ee6-4dfa-b43f-c7180a38ea51"
    ContractSDKAgent.user = user
 
    //退出登录时，通知SDK
    ContractSDKAgent.exitLogin()
```
 **注意** : APP在登录或者退出登录后，需通知SDK，SDK需根据登录状态处理相关业务行为
 
 * ContractSDKAgent其他方法/属性说明
 
| 方法/属性 | 功能 | 说明|
| -------|:-----:|:-----:|
|logEnabled |SDK日志输出开关|默认true|
|isContractCloudSDK |是否是合约云SDK|默认true|
|httpConfig |http和ws相关配置|参考demo配置即可|
|isZhEnv |设置是否是中文环境|目前只支持中英文|
|sdkListener |SDK初始化监听|只有监听初始化成功后，才能做相关业务操作|
|user |合约User对象|登陆后设置即可|
|isLogin |合约SDK是否登录||
|exitLogin |退出登录|APP退出登录，需要通知SDK退出|
|registerSDKUserStatusListener |SDK用户状态监听|主要SDK监听登录和退出登录|
--------

### 5.合约公共数据类(不需要登录):ContractPublicDataAgent,常见方法如下，

| 方法/属性 | 功能 | 说明|
| -------|:-----:|:-----:|
|getContracts |合约列表|SDK初始化成功后调用|
|getContractTickers |合约Ticker列表|同上|
|getSimulationContract |模拟合约列表||
|subscribeKlineWs |订阅K线||
|unSubscribeKlineWs |取消K线订阅||
|subscribeDepthWs |订阅深度|openScanTask，表示ws断开时，是否自动触发API扫描任务|
|unSubscribeDepthWs |取消订阅深度||
|subscribeAllTickerWebSocket |订阅所有Ticker|
|unSubscribeAllTickerWebSocket |取消订阅所有Ticker|
|subscribeTradeWs |订阅成交记录|
|unSubscribeTradeWs |取消订阅成交记录|
|registerKlineWsListener |监听K线|
|registerDepthWsListener |监听深度|
|subscribeTickerWs |订阅单个Ticker|
|unSubscribeTickerWs |取消订阅单个Ticker|
|registerTickerWsListener |注册Ticker监听|
|getContractTicker |根据合约id/coin_name获取Ticker|
|getContract |根据合约id 获取Contract|
|loadFundingRate |获取资金费率|
|loadIndexes |获取指数列表|
|loadRiskReserves |获取合约的保险金记录|
|loadContractSpot |加载合约K线|
|loadGlobalLeverage |得到全局杠杆|
|setGlobalLeverage |设置全局杠杆|
|loadContractTrades |获取合约交易记录|
--------



#### 6.合约私有数据ContractUserDataAgent：必须在登录情况下调用
| 方法/属性 | 功能 | 说明|
| -------|:-----:|:-----:|
|getContractAccounts |获取所有合约账户|asyncNet 是否异步请求API接口|
|getContractAccount |根据币种获取合约账户||
|getContractOrder |获得委托订单|目前只获取委托中的订单,历史委托需调API接口|
|getContractPlanOrder |获得计划委托订单|同上|
|getCoinPositions |获得币种/合约仓位|asyncNet 是否异步请求接口|
|getContractPosition |获得某个方向的仓位||
|registerContractAccountWsListener |注册合约账户监听|有资产变动会触发回调|
|registerContractOrderWsListener |合约订单监听|包括普通订单和计划委托订单|
|registerContractPositionWsListener |合约仓位监听|
|loadContractPosition |根据币种/合约Id加载合约仓位|
|loadContractOrder |加载合约订单|
|loadContractPlanOrder |加载合约计划订单|
|doCancelOrders |取消合约订单|
|doCancelPlanOrders |取消合约计划订单|
|doAdjustMargin |调整保证金|
|doSubmitOrder |提交普通订单|
|doSubmitPlanOrder |提交计划订单|
|doCreateContractAccount |开通合约账户|
|loadLiqRecord |获取用户爆仓记录|
|loadUserTrades |获取用户交易记录|
|loadCashBooks |获取资金流水|
|loadContractSpot |加载合约K线|
|loadGlobalLeverage |得到全局杠杆|
|setGlobalLeverage |设置全局杠杆|
|loadContractTrades |获取合约交易记录|
--------


### 7.常见问题或者说明
* SDK层对接口数据和ws逻辑进行了底层和模型的封装，具体调用和参考SDK    DEMO在UI层的调用示例，数据模型可和[接口文档](https://documenter.getpostman.com/view/4857742/SzKN2hzw?version=latest)对照
* SDK会自动感知注册监听Listener的生命周期，在组件销毁时，会自动取消注册
* 在WS断开的情况，有自动重连机制，和私有数据会自动订阅，UI层只需注册监听即可




# 简介

DefinedSessionFilter是基于java开发的拦截器，根据读取properties文件获取对应的配置，使用redis和cookie来进行存储,cookie可以选择是否使用blowfish进行加密。

# 系统需求

使用该filter必须保证maven的环境中包含以下dependency。
```
<dependency>
	<groupId>javax.servlet</groupId>
	<artifactId>servlet-api</artifactId>
	<version>2.5</version>
</dependency>
<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
	<version>2.4.1</version>
</dependency>
<dependency>
	<groupId>commons-dbcp</groupId>
	<artifactId>commons-dbcp</artifactId>
	<version>1.4</version>
</dependency>
<dependency>
	<groupId>commons-pool</groupId>
	<artifactId>commons-pool</artifactId>
	<version>1.6</version>
</dependency>
<dependency>
	<groupId>commons-configuration</groupId>
	<artifactId>commons-configuration</artifactId>
	<version>1.7</version>
</dependency>

```

# 使用方法
使用方法针对其他需要引用DefinedSessionFilter.jar的项目。
## pom.xml
```
<dependency>
<groupId>com.session</groupId><artifactId>DefinedSession</artifactId>
<version>*Version*</version>
<scope>system</scope>
<systemPath>*JARPath*</systemPath>
</dependency>
```
version为版本号，自定义。jarpath为编译后jar文件的位置，当重新打包后别忘了引用项目中需要update一下maven。

## web.xml

增加一个filter
```
<filter>
        <filter-name>SessionFilter</filter-name>
        <filter-class>
            com.session.DefinedSessionFilter
        </filter-class>
        <init-param>
            <param-name>sessionConfig</param-name>
            <param-value>*ConfigPath*</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>SessionFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```
配置文件中ConfigPath为Filter的properties文件位置。

## properties

```
domain = localhost
#cookie params:
cookie.domain = 
cookie.path = 
cookie.maxAge = 3600	
cookie.trace = false
cookie.compress = false
cookie.encrypt = false
blowfish.cipherKey = 
cookie.base64 = false
cook.compressKey =
#redis params:
redis.host = 127.0.0.1
redis.prot = 6379
redis.db = 0
redis.expiredTime = 4500
redis.nameGroup = test
```
1. cooke params：
    * domain: 
    该参数设置了当前的域名，在filter时，会检查request的来源。
    * cookie.(domain/path/maxAge)： 
    cookie的通用设置，分别代表cookie的domain，cookie的path(默认为"/")，cookie的默认时间。
    * cookie.trace:
    是否跟踪cookie的操作。
    * cookie.compress: 
    是否需要压缩cookie。
    * cookie.encrypt: 
    是否需要加密cookie。
    * blowfish.cipherKey: 
    blowfish加密的key。
    * cookie.base64: 
    是否对cookie的value进行base64编码。
    * cookie.compressKey: 
    加密cookie使用的key。

2. redis params:
    * redis.host: 
    redis server地址。
    * redis.prot: 
    redis server端口。
    * redis.db: 
    使用的redis DB。
    * redis.expiredTime: 
    redis中key的过期时间(推荐大于cookie中的值)。
    * redis.nameGroup: 
    用来区分不同的项目，该字段与生成的sessionID一起组合成了redis使用的key，所以必须保证不相同。


![Android Studio发布library到Jcenter](http://upload-images.jianshu.io/upload_images/2702499-b829cacc3324bdc6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 前言  
各位码友不知道有没有发现，不知道从何时，Android Studio的Gradle构建系统就已经把Jcenter作为默认的远程仓库了，如下：  

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.2'
    }
}
```

这样做的好处就是我们只需要在dependencies脚本块中加入对应lib即可使用远程仓库，而不需要再像eclipse那样苦苦寻找jar包。

```gradle
dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:23.+'
}
```

你应该在github上看到过要使用别人的开源库只需要往dependencies脚本块中添加一行代码即可：  

![github](http://upload-images.jianshu.io/upload_images/2702499-42abe9c7fae76121.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

假想一下，别人往他们的build.gradle中添加了一行代码就可以使用你的开源库了，这是一件多么令人兴奋的事情。那么接下来我们就把假象变为现实，学习一下怎么将自己的Android Studio Library提交到Jcenter供他人使用。

-----

# Bintray
首先我们要了解一个叫做[bintray](https://bintray.com/)的网站，它和github类似也是用来管理文件的，只不过它管理的是二进制文件，Jcenter就是它众多仓库中的一员。

首先需要注册成为这个网站的用户，接着进入profile界面：
![profile](http://upload-images.jianshu.io/upload_images/2702499-8edca1463c8d4c4d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

点击Edit


![Edit](http://upload-images.jianshu.io/upload_images/2702499-0a52b0f26a391f0e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

选择API Key
![API Key](http://upload-images.jianshu.io/upload_images/2702499-435ac7a15ac873a9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这里的API Key和你之前注册的用户名在后面会用到。

-----
# 配置
接着我们新建一个叫做CommitToJcenter的Android工程，并且添加一个叫做jad的android library模块,这个jad就是我们后面要提交到Jcenter的模块。


![project](http://upload-images.jianshu.io/upload_images/2702499-1b9486f540a998d0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

然后在工程目录下local.properties中添加如下内容：

```properties
bintray.user=***
bintray.apikey=***
```
上面的usert填你注册时候的用户名，apikey就是上面拿到的那个。

接着在工程目录下的**build.gradle**中添加如下内容：

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.2'

        //添加一下两个classpath
        // for jcenter
        // version: https://bintray.com/jfrog/jfrog-jars/gradle-bintray-plugin
        classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.7.1'
        // version: https://bintray.com/dcendents/gradle-plugins/com.github.dcendents%3Aandroid-maven-gradle-plugin
        classpath 'com.github.dcendents:android-maven-gradle-plugin:1.3'
    }
}

...
```

接着进入**jad**目录，在**build.gradle**中添加如下内容：

```gradle
...

apply plugin: 'com.github.dcendents.android-maven'
apply plugin: 'com.jfrog.bintray'

version = "1.0" //版本号，每次提交到Jcenter都要修改
def siteUrl = *** // project homepage
def gitUrl = ***  // project git
group = "com.zql.android"

install {
    repositories.mavenInstaller {
        // This generates POM.xml with proper parameters
        pom {
            project {
                packaging 'aar'
                name 'Just a demo for jcenter'
                url siteUrl
                licenses {
                    license {
                        name 'The Apache Software License, Version 2.0'
                        url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }
                developers {
                    developer {
                        id *** //Jcenter id
                        name *** //Jcenter name
                        email *** //e-mail
                    }
                }
                scm {
                    connection gitUrl
                    developerConnection gitUrl
                    url siteUrl
                }
            }
        }
    }
}


task sourcesJar(type: Jar) {
    from android.sourceSets.main.java.srcDirs
    classifier = 'sources'
}


task javadoc(type: Javadoc) {
    source = android.sourceSets.main.java.srcDirs
    classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
}


task javadocJar(type: Jar, dependsOn: javadoc) {
    classifier = 'javadoc'
    from javadoc.destinationDir
}

artifacts {
    archives javadocJar
    archives sourcesJar
}

Properties properties = new Properties()
properties.load(project.rootProject.file('local.properties').newDataInputStream())
bintray {
    user = properties.getProperty("bintray.user")
    key = properties.getProperty("bintray.apikey")
    configurations = ['archives']
    pkg {
        repo = "maven"
        name = "jad" // project name in jcenter
        websiteUrl = siteUrl
        vcsUrl = gitUrl
        licenses = ["Apache-2.0"]
        publish = true
    }
}
```

当做完以上几步后所有的配置工作就完成了，这时候选择sync一下你的工程，建议在翻墙的情况下sync，成功率会高一点。

# 提交
接着在Gradle窗口找到如下几个task，并执行：
- javadocJar
- sourcesJar
- install
- bintrayUpload


![gradle](http://upload-images.jianshu.io/upload_images/2702499-533046f6c436da02.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![gradle](http://upload-images.jianshu.io/upload_images/2702499-31c2fe030fd96b4f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

提交结束后我们再次打开[bintray](https://bintray.com/)，在profile的last activity中可以看到这样的信息：


![last activity](http://upload-images.jianshu.io/upload_images/2702499-4fdebda66957ce39.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

点击jad进入模块界面,点击add to Jcenter：


![add to jcenter](http://upload-images.jianshu.io/upload_images/2702499-373dbbe4e6c717de.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

接着填写一些相关申请信息即可，记得得是英文。我目前的通过率是100%。

![request](http://upload-images.jianshu.io/upload_images/2702499-13f6fdab4fb08960.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

接着就是等待了，差不多一天时间就能得到答复。


![response](http://upload-images.jianshu.io/upload_images/2702499-f9ffbca647f83632.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

OK,进去了，这时候我们已经可以在其他项目中使用我们提交的模块了。

# 使用

上面我们新建了jad模块就直接提交了，并没有写代码，所以也不好在其他工程测试。接下来我们添加一个类JAD.java

```java
package android.zql.com.jad;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by scott on 8/11/16.
 */
public final class JAD {

    public static final void sayHello(Context context){
        Toast.makeText(context,"hello man",Toast.LENGTH_LONG).show();
    }
}
```
很简单，使用Toast弹出Hello man这句话。
由于第一次已经被允许加入Jcenter了，所以以后的提交都会默认加入Jcenter。接着我们修改一下这次我们需要提交的版本号为1.0.1：

```gradle
apply plugin: 'com.github.dcendents.android-maven'
apply plugin: 'com.jfrog.bintray'
version = "1.0.1"
```
然后执行以下四步：
- javadocJar
- sourcesJar
- install
- bintrayUpload

执行完成后我们看下[bintray](https://bintray.com/)中的jad:

我们刚提交的1.0.1已经出现在这里了，
![1.0.0](http://upload-images.jianshu.io/upload_images/2702499-2b0ea00c02876595.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

这边给出了jad在gradle中的使用方法，是不是很熟悉？
![gradle usage](http://upload-images.jianshu.io/upload_images/2702499-94200416061d1072.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

下面我们就在项目中使用jad吧，在项目的build.gradle中添加如下代码：
```gradle
dependencies {
    ...
    compile 'com.zql.android:jad:1.0.1'
}
```
在Activity中使用：
```java
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JAD.sayHello(this);
    }
```
运行结果如下：

![运行结果](http://upload-images.jianshu.io/upload_images/2702499-cece63e6f12e99e1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 小结
最后献上这个CommitToJcenter的[github地址](https://github.com/ZhangQinglian/CommitToJcenter)。

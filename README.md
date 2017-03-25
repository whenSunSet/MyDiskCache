> 转载请注明出处
> **Fresco源代码文档翻译项目请看这里：[FrescoFresco源代码文档翻译项目](https://github.com/whenSunSet/MyFresco/tree/master)** 
> 硬盘缓存是android图片框架中比较重要的一个模块，Fresco中自己重写了一个硬盘缓存框架，代替了android本身的DiskLruCache，所以今天我们就来介绍Fresco中的硬盘缓存，并且将其提取出来成为我们自己的框架。**我已经成功提取出了 Fresco 中的硬盘缓存框架，这是项目地址[Frsco硬盘缓存框架项目地址](https://github.com/whenSunSet/MyDiskCache/tree/master)，建议大家在看文章的时候结合项目代码，项目中的每个class文件中都有注释，看起来还是比较容易的。**

## 一、目录介绍 ##
- 1.binaryResource包：这里面有一个接口和一个类，Fresco遵循面向接口编程这一原则，所以很多地方都会用接口来增强可扩展性。总的来说BinaryResource这个接口代表一个字节序列，它抽象了底层的资源，比如一个file文件。FileBinaryResource内部就有一个File成员，以方便对File进行操作。
- 2.cacheEventAndListenner包：在硬盘缓存的过程中，会有许多的事件发生，比如查找缓存时候命中了、要插入一个缓存、读取缓存失败了等等。此时客户端会需要一个监听器来监听各种硬盘缓存事件的发生。此时CacheEventListener接口就可以当客户端对硬盘缓存的监听器，CacheEventListener中的每个方法中都传入了一个CacheEvent。CacheEvent是一个接口，其实现类中包裹了许多与硬盘缓存相关的东西，SettableCacheEvent就是CacheEvent的唯一实现类，由于硬盘缓存事件产生的很频繁，所以SettableCacheEvent使用了享元模式(在内存中只维持两个对象，不断的回收重用)。CacheErrorLogger接口是硬盘缓存的读或写产生异常的时候使用到的类。CacheEventListener和CacheErrorLogger都只有一个空实现用来填充代码，具体的实现需要使用者实现。
- 3.cacheKey包：但我们插入或者取出一个文件缓存的时候就会用到CacheKey，SimpleCacheKey的内部其实就是一个String，MultiCacheKey的内部只是一系列CacheKey的集合。我们一般使用的是SimpleCacheKey，因为我们去网络加载图片的时候Uri就是一个最好的key。
- 4.comparator包：这里的EntryEvictionComparator实现了Comparator<DiskStorage.Entry>，在后面我们会知道DiskStorage.Entry表示一条文件缓存。所以这个接口是为了比较每一条文件缓存。EntryEvictionComparatorSupplier使用提供者模式，在get()中返回了一个EntryEvictionComparator，所以只需要实现EntryEvictionComparatorSupplier,在get()中返回EntryEvictionComparator的具体实现，我们就可以定义一个DiskStorage.Entry的比较器。目前Fresco中自定义了两个比较器DefaultEntryEvictionComparatorSupplier和ScoreBasedEvictionComparatorSupplier，他们分别是基于LRU和权重的比较器。
- 5.fileTree包：硬盘缓存会使用到文件系统，所以此时对一个目录所有文件的遍历是必不可少的，这里FileTree负责遍历一个目录下的所有文件和提供一个安全的删除文件夹方式，其遍历的时候将每个文件交与FileTreeVisitor进行处理。这是一个很好的访问文件系统的方式。FileTreeVisitor在DiskStorageCache中有两个具体实现。
- 6.trimmable包：在硬盘缓存中有一个类会直接负责和硬盘上的文件打交道，所以其会使用了大量硬盘空间，一旦硬盘空间不足的话，可能造成硬盘缓存失败的情况。所以此时我们可以将该class实现DiskTrimmable接口，然后在DiskTrimmableRegistry的实现类中注册，一旦硬盘空间不够了，使用DiskTrimmableRegistry实现类通知所有实现了DiskTrimmable并且注册过的对象，让该对象删除缓存文件以释放硬盘空间，这里的释放规则是LRU。Fresco中只有一个NoOpDiskTrimmableRegistry空
实现，这里需要让使用者自己去实现更符合自己app的通知方式。 
- 7.util包：这个包中放了一些工具类，大家有兴趣可以去看看
- 8.core包：前面7个包都是辅助的包，Fresco真正的硬盘缓存核心类在这个包中，这里DiskStorage接口的实现类负责和android的文件系统直接打交道，提供缓存的文件的增删改api。FileCache接口的实现类则是负责缓存的逻辑，比如缓存满了的清除逻辑。FileCache接口的实现类直接持有DiskStorage接口的引用以操作文件系统。**我们接下来要看的就是这个包中的类**

## 二、硬盘缓存核心类分析 ##
先上一张图，让大家简单了解各个接口提供的api。
![核心类关系图](http://img.blog.csdn.net/20170323194454288?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYTEwMTg5OTg2MzI=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### 1.DefaultDiskStorage ###
> 这个class的代码就不贴了，强烈建议读者把我前面的项目下载下来，结合博客一起观看。

这个类是DiskStorage接口的实现类，前面说了这个类是直接与android的文件系统打交道的类。这个类有以下几个功能特点：

- 1.该类构造函数中在传入的缓存根目录(下面称该文件夹为cache)下创建一个当前缓存版本的文件夹，接下来该对象经手的缓存文件都储存在这里文件夹中，我们在后面称这个文件夹为 version1.0。
- 2.三星的老手机有一个问题，就是一个文件夹下面不能放置过多的文件，这个问题被称为RFS。因此在每次储存缓存文件的时候会将 缓存文件key的hash值对100取模，这个值就是文件夹的名字，如果这个文件夹没创建就创建一个，然后将缓存文件放入其中。在取缓存文件的时候也要经历这个流程。
- 3.缓存文件在插入的时候，有两个步骤1.将该文件写成.tmp后缀的临时文件，此时该文件对使用者不可见。2.将.tmp后缀的文件改名为.cnt后缀的文件，此时该文件对使用者可见。
- 4.该对象在两种情况下需要使用到FileTree：
	- 1.清理不需要的文件(如tmp文件或不是本版本的文件)，此时会调用purgeUnexpectedResources()使用FileTree.walkFileTree()遍历cache文件夹，然后使用FileTreeVisitor的实现类PurgingVisitor对每个文件进行判断，看看是否需要清除该文件
	- 2.获取Entry：由上面的图中我们可以看见Entry是DiskStorage的一个内部接口，DefaultDiskStorage中用EntryImpl实现了它，而每一个Entry就代表着一个文件缓存。所以要获取所有的缓存信息就应该遍历 version1.0。而这里使用的FileTree.walkFileTree()和EntriesCollector。
- 5.硬盘缓存的清理算法一般使用的都是LRU，所以在每次调用getResource()获取文件缓存的时候，都会将该文件的LastModified设置成目前的时间以便在后面进行缓存的清理。
- 6.我们从前面的图中看见DiskStorage接口insert()方法返回了一个Inserter，这个Inserter在本class中被实现了。使用这种方式是为了进行并发地写入多条缓存条目。在Insert.commit()调用之前，这个缓存条目对客户端是不可见的。
- 7.除上面的功能，本class还提供了通过Key和Entry删除缓存、清理所有缓存、通过Key查询文件缓存这些功能。

### 2.DiskStorageCache ###
> 同样不贴代码，再次建议大家下载项目代码观看博客。

这个类是FileCache的实现类，其通过DefaultDiskStorage与android文件系统打交道，并且处理文件缓存的各种逻辑。来说说它的功能特点：

- 1.这个对象是CacheEventListener监听的对象，用户可以传入一个自己的Listenner来监听硬盘缓存的各种活动，比如插入缓存、删除缓存、缓存失败等等事件。
- 2.在该类中有一个CacheStats内部类(用于储存该对象已经使用的硬盘空间，以及缓存条目数量)和一个HashSet(mResourceIndex用于储存所有缓存条目的Id)，由于在Fresco中硬盘缓存使用是很频繁的，所以如果实时刷新这两个东西是不值得的，因此该类中设置了一个时间(FILECACHE_SIZE_UPDATE_PERIOD_MS),每隔这么多时间上面两个对象的状态就会被刷新。
- 3.该对象可以在getResource(CacheKey)方法中通过DefaultDiskStorage#getResource()来获取传入key所对应的缓存文件，每次调用这个方法都会刷新该缓存文件的时间戳，为之后的LRU删除缓存做准备
- 4.该对象可以在probe(CacheKey)方法中通过DefaultDiskStorage#touch()查询传入Key的缓存文件是否存在，如果存在，同样会改变该缓存文件的时间戳。
- 5.使用者可以调用该对象的insert(CacheKey，WriterCallback)方法插入一个缓存文件，这里需要在使用者覆盖WriterCallback的write(OutputStream)方法，通过OutputStream将需要储存的数据写入缓存文件。instert(CacheKey，WriterCallback)方法中会创建一个缓存文件的OutputStream，然后将其传入WriterCallback#get()中以供使用者使用。
- 6.在remove(CacheKey)会通过DefaultDiskStorage#remove()方法，删除传入key对应的缓存文件，同时会删除mResourceIndex中该缓存文件的id。
- 7.clearOldEntries(long cacheExpirationMs)会通过DefaultDiskStorage#getEntries()和DefaultDiskStorage#remove()这两个方法删除比传入时间cacheExpirationMs老的缓存文件。
- 8.maybeEvictFilesInCacheDir()会在插入一个缓存文件之前，判断本对象使用的硬盘空间是否已经超过初始化时设置的限制，如果超过限制就会调用evictAboveSize()(先在getSortedEntries()方法中通过DefaultEntryEvictionComparatorSupplier将所有Entry排序，然后按时间顺序一个个删除缓存文件，直至达到缓存空间要求)。
- 9.hasKeySync(CacheKey)在mResourceIndex中判断该文件缓存是否存在，由于该对象不是实时刷新，所以会出现滞后性即有些文件缓存已经存在，但是mResourceIndex中并没有其id。
- 10.hasKey(CacheKey)该方法是hasKeySync()的升级版，其不仅会通过hasKeySync()在mResourceIndex查找，如果没找到还会通过DefaultDiskStorage#contains()方法查找，这样也提高了查找效率。
- 11.由于该class实现了DiskTrimmable接口，所以其在硬盘空间吃紧的时候也会调用evictAboveSize()进行缓存文件的清理，不过Fresco中DiskTrimmableRegistry的默认实现是NoOpDiskTrimmableRegistry，这个实现中不会做任何事情。所以具体的监听需要使用者来做。

## 三、Fresco硬盘缓存框架的使用 ##
> Fresco在使用硬盘缓存框架的时候，与其他模块通信的时候使用了两个类DiskCacheConfig和DiskStorageCache。DiskCacheConfig很好理解，负责用Builder模式创建一个DiskStorageCache。所以这里归更到底就是使用DiskStorageCache暴露出来的增删改查等api，而硬盘缓存框架中的其他类与Fresco的其他模块是解耦的，这也是软件工程中的一个重要的思想。所以接下来我们就来使用一下DiskStorageCache，也算是对这篇博客的总结。

这里代码不多所以贴下代码：

    public class MainActivity extends AppCompatActivity {
    FileCache mFileCache;
    Button buttonInsert;
    Button buttonHasKey;
    Button buttonRemove;
    Button buttonClearAll;
    Button buttonGetCache;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDiskCache();
        initView();

        final int[] times = {0};
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleCacheKey simpleCacheKey=new SimpleCacheKey(String.valueOf(times[0]));
                times[0]++;
                insert(simpleCacheKey);
            }
        });

        buttonHasKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "key 5 是否存在？" + mFileCache.hasKey(new SimpleCacheKey("5")), Toast.LENGTH_SHORT).show();
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                times[0]--;
                SimpleCacheKey simpleCacheKey=new SimpleCacheKey(String.valueOf(times[0]));
                mFileCache.remove(simpleCacheKey);
            }
        });

        buttonClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileCache.clearAll();
                times[0]=0;
            }
        });

        buttonGetCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCache();
            }
        });
    }

    private void initDiskCache(){

        DiskCacheConfig diskCacheConfig=DiskCacheConfig.newBuilder(this).build();
        Toast.makeText(this, "缓存文件夹："+diskCacheConfig.getBaseDirectoryPathSupplier().get().getPath(), Toast.LENGTH_SHORT).show();
        DefaultDiskStorage defaultDiskStorage=new DefaultDiskStorage(
                diskCacheConfig.getBaseDirectoryPathSupplier().get(),
                diskCacheConfig.getVersion(),
                diskCacheConfig.getCacheErrorLogger());

        DiskStorageCache.Params params = new DiskStorageCache.Params(
                diskCacheConfig.getMinimumSizeLimit(),
                diskCacheConfig.getLowDiskSpaceSizeLimit(),
                diskCacheConfig.getDefaultSizeLimit());


        CacheEventListener cacheEventListener=new CacheEventListener() {
            @Override
            public void onHit(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache hit", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onMiss(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache miss", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onWriteAttempt(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache write start", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onWriteSuccess(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache write success", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onReadException(CacheEvent cacheEvent) {
                Log.d("MainActivity Cache ReadException", cacheEvent.getCacheKey().getUriString());
            }

            @Override
            public void onWriteException(CacheEvent cacheEvent) {
                Log.d("MainActivity Cache WriteException", cacheEvent.getCacheKey().getUriString());
            }

            @Override
            public void onEviction(CacheEvent cacheEvent) throws IOException {
                Log.d("MainActivity Cache Eviction", cacheEvent.getCacheKey().getUriString());
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }

            @Override
            public void onCleared() throws IOException {
                Log.d("MainActivity", "Cleared");
                Log.d("MainActivity", "mFileCache.getDumpInfo():" + mFileCache.getDumpInfo());
            }
        };

        mFileCache=new DiskStorageCache(
                defaultDiskStorage,
                diskCacheConfig.getEntryEvictionComparatorSupplier(),
                params,
                cacheEventListener,
                diskCacheConfig.getCacheErrorLogger(),
                diskCacheConfig.getDiskTrimmableRegistry(),
                diskCacheConfig.getContext(),
                Executors.newSingleThreadExecutor(),
                diskCacheConfig.getIndexPopulateAtStartupEnabled());

    }

    private void initView(){
        buttonInsert=(Button)findViewById(R.id.insert);
        buttonHasKey=(Button)findViewById(R.id.hasKey);
        buttonRemove=(Button)findViewById(R.id.remove);
        buttonClearAll=(Button)findViewById(R.id.clearAll);
        buttonGetCache=(Button)findViewById(R.id.getCache);
        imageView=(ImageView)findViewById(R.id.image);
    }

    private void insert(SimpleCacheKey simpleCacheKey){
        try {
            mFileCache.insert(simpleCacheKey, new WriterCallback() {
                @Override
                public void write(OutputStream os) throws IOException {
                    FileUtils.bitmapToFile(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher),os);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCache(){
        BinaryResource diskCacheResource = mFileCache.getResource(new SimpleCacheKey("2"));
        if (diskCacheResource==null) Toast.makeText(this, "miss 2", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this, "hit 2", Toast.LENGTH_SHORT).show();
            try {
                imageView.setImageBitmap(BitmapFactory.decodeStream(diskCacheResource.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	}

- 1.首先是使用DiskCacheConfig初始化DiskStorageCache：
	- 1.由于DiskCacheConfig采用的是Bulider模式，所以Builder中内置了许多默认属性。如果我们没有特殊要求可以直接获取一个DiskCacheConfig对象。
	- 2.通过DiskCacheConfig中的默认属性创建DefaultDiskStorage和DiskStorageCache.Params以供后面使用。
	- 3.我们可以选择创建一个CacheEventListener以在客户端监听硬盘缓存的行为。当然也可以选择不监听。
	- 4.最后使用上面的属性创建一个FileCache，因为解耦设计，我们只需要获取一个FileCache而不是DiskStorageCache。这里要注意一下，**这个硬盘缓存框架没有内置线程池**，而一般来说对于硬盘的读写都是在其他线程中的，Fresco中就是创建了一个读线程池和一个写线程池将DiskStorageCache的读写操作放在其中操作。我这里为了方便就直接在UI线程进行操作了。大家在使用的时候需要根据自己的需求创建线程池。此外我们注意到在FileCache的创建过程中传入了一个Executors.newSingleThreadExecutor()，注意这不是用于读写操作的，大家进入DiskStorageCache的构造器中可以看见，这个线程池根本没有被保存为成员变量，只是用于做了一些初始化的操作。
- 2.初始化了5个Button和一个ImageView
- 3.第一个按钮是插入硬盘缓存的按钮：可以看见我使用了SimpleCacheKey以从0递增的String作为缓存的id。然后在insert()方法中调用了FileCache#insert(),其中通过WriterCallback#get()提供的OutputStream将R.mipmap.ic_launcher写入了硬盘缓存中。
- 4.第二个按钮是判断是否有给定的缓存：我调用FileCache#hasKey()判断id为5的缓存是否存在。
- 5.第三个按钮是通过id删除硬盘缓存：调用的是FileCache.remove()方法
- 6.第四个按钮是清空缓存：通过FileCache.clearAll()实现；
- 7.第五个按钮显示id为2的缓存图片：在getCache()中通过FileCache.getResource()获取了一个BinaryResource，这个类实际上是FileBinaryResource，可以直接获取其InputStream来显示图片。

以上就是Fresco硬盘缓存框架的使用。

## 四、总结
 
> Fresco的硬盘缓存框架，还是挺有趣的，其中用到了许多软件工程的思想与Java设计模式。Fresco中还有许多模块非常有趣，做个预告**下一篇博客将会分析Fresco的内存缓存框架**有兴趣的同学一定别错过了。
package com.emis.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletContext;

import com.emis.db.emisProp;
import com.emis.file.emisFile;
import com.emis.report.emisString;

/**
 * 补空，检查长度,检查字符,繁简自动转入,难字mapping输出，<br>
 * checkLength()检查字符串长度，效能调整，不做简繁检查 提供取字符串<br>
 * 部分方法来自emisString<br> 
 * 据个别作业需要，提供下述方法<br>
 * 提供检查全字符，简，繁体字符，中文字符检查函数<br>
 * isDoubleChartSet() 双字节检查，不做简繁检查<br>
 * complTosimpleNoMappingBig5() 输出简体编码,不进行简繁转换 <br>
 * complToMS950NoMappingGBK(); 输出繁体编码，不进行繁简转换<br>
 * complToMS950()输出繁体编码，自动繁简转换，个别作业使用<br>
 * complTosimple() 输出繁体编码，自动繁简转换，个别作业使用<br>
 * isSimpleChineseCharacter()简体字检查，适用于complTosimple()方法调用<br>
 * isTraditionalChineseCharacter()繁体字检查，适用于complToMS950()方法调用<br>
 * 创建输出文件类型 createWriter()<br>
 * @version 2010/02/04 [14335] Wing 不做简繁互转 添加下载方法 complTosimpleNoMappingBig5
 *          ,complToMS950NoMappingGBK添加, MS950,GBK
 * @version [14291 ] 2010/02/05 wing padding需要补回超長則不處理
 * @version 2010/02/06 wing 效能及方法调用整理，注解整理,检查长度使用checkLength,其它方法做特别需要处理 <br>
 *          删除complFilterNoUTF8CharsetToMS950    <br>
 *          删除isOnlyTraditionalChineseCharacter  <br>
 * Track+[15019] dana.gao 2010/06/03 在檢查是否為基本字符方法,增加是否為全角數字(符號)判斷.
 */
public class emisUTF8StringUtil {

	/**
	 * 定义常量，输出GBK档案
	 */
	final public static String TURNOUT_GBK = "GBK";

	/**
	 * 定义常量，输出GB2312档案
	 */
	final public static String TURNOUT_GB2312 = "GB2312";

	/**
	 * 定义常量，输出UTF-8档案
	 */
	final public static String TURNOUT_UTF_8 = "UTF-8";

	/**
	 * 定义常量，输出MS950档案
	 */
	final public static String TURNOUT_MS950 = "MS950";

	/**
	 * 定义常量,空格字符定义
	 */
	final public static char SPACE_SIGN = ' ';

	/**
	 * 字符串对齐方式:左
	 */
	public static final int A_LEFT = 1;

	/**
	 * 字符串对齐方式:右
	 */
	public static final int A_CENTER = 2;

	/**
	 * 字符串对齐方式:居中
	 */
	public static final int A_RIGHT = 3;

	/**
	 * 繁体字char数据,必须用utf-8方式保存char ，通过对比实现 可修改为保存在UTF-8格式外部文件读入，自定义mapping
	 */
	public static String turnCompl = "啊阿埃挨哎唉哀皚癌藹矮艾礙愛隘鞍氨安俺按暗岸胺案骯昂盎凹敖熬翱襖傲奧懊澳芭捌扒叭吧笆八疤巴拔跋靶把耙壩霸罷爸白柏百擺佰敗拜稗斑班搬扳般頒板版扮拌伴瓣半辦絆邦幫梆榜膀綁棒磅蚌鎊傍謗苞胞包褒剝薄雹保堡飽寶抱報暴豹鮑爆杯碑悲卑北輩背貝鋇倍狽備憊焙被奔苯本笨崩繃甭泵蹦迸逼鼻比鄙筆彼碧蓖蔽畢斃毖幣庇痺閉敝弊必辟壁臂避陛鞭邊編貶扁便變卞辨辯辮遍標彪膘表鱉憋別癟彬斌瀕濱賓擯兵冰柄丙秉餅炳病並玻菠播撥缽波博勃搏鉑箔伯帛舶脖膊渤泊駁捕卜哺補埠不布步簿部怖擦猜裁材才財睬踩採彩菜蔡餐參蠶殘慚慘燦蒼艙倉滄藏操糙槽曹草廁策側冊測層蹭插叉茬茶查碴搽察岔差詫拆柴豺攙摻蟬饞讒纏鏟產闡顫昌猖場嘗常長償腸廠敞暢唱倡超抄鈔朝嘲潮巢吵炒車扯撤掣徹澈郴臣辰塵晨忱沉陳趁襯撐稱城橙成呈乘程懲澄誠承逞騁秤吃痴持匙池遲弛馳恥齒侈尺赤翅斥熾充沖虫崇寵抽酬疇躊稠愁籌仇綢瞅丑臭初出櫥廚躇鋤雛滁除楚礎儲矗搐觸處揣川穿椽傳船喘串瘡窗幢床闖創吹炊捶錘垂春椿醇唇淳純蠢戳綽疵茨磁雌辭慈瓷詞此刺賜次聰蔥囪匆從叢湊粗醋簇促躥篡竄摧崔催脆瘁粹淬翠村存寸磋撮搓措挫錯搭達答瘩打大呆歹傣戴帶殆代貸袋待逮怠耽擔丹單鄲撣膽旦氮但憚淡誕彈蛋當擋黨蕩檔刀搗蹈倒島禱導到稻悼道盜德得的蹬燈登等瞪凳鄧堤低滴迪敵笛狄滌翟嫡抵底地蒂第帝弟遞締顛掂滇碘點典靛墊電佃甸店惦奠澱殿碉叼雕凋刁掉吊釣調跌爹碟蝶迭諜疊丁盯叮釘頂鼎錠定訂丟東冬董懂動棟侗恫凍洞兜抖斗陡豆逗痘都督毒犢獨讀堵睹賭杜鍍肚度渡妒端短鍛段斷緞堆兌隊對墩噸蹲敦頓囤鈍盾遁掇哆多奪垛躲朵跺舵剁惰墮蛾峨鵝俄額訛娥惡厄扼遏鄂餓恩而兒耳爾餌洱二貳發罰筏伐乏閥法琺藩帆番翻樊礬釩繁凡煩反返范販犯飯泛坊芳方肪房防妨仿訪紡放菲非啡飛肥匪誹吠肺廢沸費芬酚吩氛分紛墳焚汾粉奮份忿憤糞豐封楓蜂峰鋒風瘋烽逢馮縫諷奉鳳佛否夫敷膚孵扶拂輻幅氟符伏俘服浮涪福袱弗甫撫輔俯釜斧脯腑府腐赴副覆賦復傅付阜父腹負富訃附婦縛咐噶嘎該改概鈣蓋溉干甘杆柑竿肝趕感稈敢贛岡剛鋼缸肛綱崗港杠篙皋高膏羔糕搞鎬稿告哥歌擱戈鴿胳疙割革葛格蛤閣隔鉻個各給根跟耕更庚羹埂耿梗工攻功恭龔供躬公宮弓鞏汞拱貢共鉤勾溝苟狗垢構購夠辜菇咕箍估沽孤姑鼓古蠱骨谷股故顧固雇刮瓜剮寡挂褂乖拐怪棺關官冠觀管館罐慣灌貫光廣逛瑰規圭硅歸龜閨軌鬼詭癸桂櫃跪貴劊輥滾棍鍋郭國果裹過哈骸孩海氦亥害駭酣憨邯韓含涵寒函喊罕翰撼捍旱憾悍焊汗漢夯杭航壕嚎豪毫郝好耗號浩呵喝荷菏核禾和何合盒貉閡河涸赫褐鶴賀嘿黑痕很狠恨哼亨橫衡恆轟哄烘虹鴻洪宏弘紅喉侯猴吼厚候後呼乎忽瑚壺葫胡蝴狐糊湖弧虎唬護互滬戶花嘩華猾滑畫劃化話槐徊懷淮壞歡環桓還緩換患喚瘓豢煥渙宦幻荒慌黃磺蝗簧皇凰惶煌晃幌恍謊灰揮輝徽恢蛔回毀悔慧卉惠晦賄穢會燴匯諱誨繪葷昏婚魂渾混豁活伙火獲或惑霍貨禍擊圾基機畸稽積箕肌飢跡激譏雞姬績緝吉極棘輯籍集及急疾汲即嫉級擠幾脊己薊技冀季伎祭劑悸濟寄寂計記既忌際妓繼紀嘉枷夾佳家加莢頰賈甲鉀假稼價架駕嫁殲監堅尖箋間煎兼肩艱奸緘繭檢柬鹼鹼揀撿簡儉剪減薦檻鑒踐賤見鍵箭件健艦劍餞漸濺澗建僵姜將漿江疆蔣槳獎講匠醬降蕉椒礁焦膠交郊澆驕嬌嚼攪鉸矯僥腳狡角餃繳絞剿教酵轎較叫窖揭接皆秸街階截劫節桔杰捷睫竭潔結解姐戒藉芥界借介疥誡屆巾筋斤金今津襟緊錦僅謹進靳晉禁近燼浸盡勁荊兢莖睛晶鯨京驚精粳經井警景頸靜境敬鏡徑痙靖竟競淨炯窘揪究糾玖韭久灸九酒廄救舊臼舅咎就疚鞠拘狙疽居駒菊局咀矩舉沮聚拒據巨具距踞鋸俱句懼炬劇捐鵑娟倦眷卷絹撅攫抉掘倔爵覺決訣絕均菌鈞軍君峻俊竣浚郡駿喀咖卡咯開揩楷凱慨刊堪勘坎砍看康慷糠扛抗亢炕考拷烤靠坷苛柯棵磕顆科殼咳可渴克刻客課肯啃墾懇坑吭空恐孔控摳口扣寇枯哭窟苦酷庫褲夸垮挎跨胯塊筷儈快寬款匡筐狂框礦眶曠況虧盔巋窺葵奎魁傀饋愧潰坤昆捆困括擴廓闊垃拉喇蠟臘辣啦萊來賴藍婪欄攔籃闌蘭瀾讕攬覽懶纜爛濫琅榔狼廊郎朗浪撈勞牢老佬姥酪烙澇勒樂雷鐳蕾磊累儡壘擂肋類淚棱楞冷厘梨犁黎籬狸離漓理李裡鯉禮莉荔吏栗麗厲勵礫歷利例俐痢立粒瀝隸力璃哩倆聯蓮連鐮廉憐漣帘斂臉鏈戀煉練糧涼梁粱良兩輛量晾亮諒撩聊僚療燎寥遼潦了撂鐐廖料列裂烈劣獵琳林磷霖臨鄰鱗淋凜賃吝拎玲菱零齡鈴伶羚凌靈陵嶺領另令溜琉榴硫餾留劉瘤流柳六龍聾嚨籠窿隆壟攏隴樓婁摟簍漏陋蘆盧顱廬爐擄鹵虜魯麓碌露路賂鹿潞祿錄陸戮驢呂鋁侶旅履屢縷慮氯律率濾綠巒攣孿灤卵亂掠略掄輪倫侖淪綸論蘿螺羅邏鑼籮騾裸落洛駱絡媽麻瑪碼螞馬罵嘛嗎埋買麥賣邁脈瞞饅蠻滿蔓曼慢漫謾芒茫盲氓忙莽貓茅錨毛矛鉚卯茂冒帽貌貿麼玫枚梅霉煤沒眉媒鎂每美昧寐妹媚門悶們萌蒙檬盟錳猛夢孟瞇醚靡糜迷謎彌米秘覓泌蜜密冪棉眠綿冕免勉娩緬面苗描瞄藐秒渺廟妙蔑滅民抿皿敏憫閩明螟鳴銘名命謬摸摹蘑模膜磨摩魔抹末莫墨默沫漠寞陌謀牟某拇牡畝姆母墓暮幕募慕木目睦牧穆拿哪吶鈉那娜納氖乃奶耐奈南男難囊撓腦惱鬧淖呢餒內嫩能妮霓倪泥尼擬你匿膩逆溺蔫拈年碾攆捻念娘釀鳥尿捏聶孽嚙鑷鎳涅您檸獰凝寧擰濘牛扭鈕紐膿濃農弄奴努怒女暖虐瘧挪懦糯諾哦歐鷗毆藕嘔偶漚啪趴爬帕怕琶拍排牌徘湃派攀潘盤磐盼畔判叛乓龐旁耪胖拋咆刨炮袍跑泡呸胚培裴賠陪配佩沛噴盆砰抨烹澎彭蓬棚硼篷膨朋鵬捧碰坯砒霹批披劈琵毗啤脾疲皮匹痞僻屁譬篇偏片騙飄漂瓢票撇瞥拼頻貧品聘乒坪蘋萍平憑瓶評屏坡潑頗婆破魄迫粕剖扑鋪仆莆葡菩蒲埔朴圃普浦譜曝瀑期欺棲戚妻七淒漆柒沏其棋奇歧畦崎臍齊旗祈祁騎起豈乞企啟契砌器氣迄棄汽泣訖掐恰洽牽扦鉛千遷簽仟謙乾黔錢鉗前潛遣淺譴塹嵌欠歉槍嗆腔羌牆薔強搶橇鍬敲悄橋瞧喬僑巧鞘撬翹峭俏竅切茄且怯竊欽侵親秦琴勤芹擒禽寢沁青輕氫傾卿清擎晴氰情頃請慶瓊窮秋丘邱球求囚酋泅趨區蛆曲軀屈驅渠取娶齲趣去圈顴權醛泉全痊拳犬券勸缺炔瘸卻鵲榷確雀裙群然燃冉染瓤壤攘嚷讓饒擾繞惹熱壬仁人忍韌任認刃妊紉扔仍日戎茸蓉榮融熔溶容絨冗揉柔肉茹蠕儒孺如辱乳汝入褥軟阮蕊瑞銳閏潤若弱撒洒薩腮鰓塞賽三三傘散桑嗓喪搔騷掃嫂瑟色澀森僧莎砂殺剎沙紗傻啥煞篩晒珊苫杉山刪煽衫閃陝擅贍膳善汕扇繕傷商賞晌上尚裳梢捎稍燒芍勺韶少哨邵紹奢賒蛇舌舍赦攝射懾涉社設砷申呻伸身深娠紳神沈審嬸甚腎慎滲聲生甥牲升繩省盛剩勝聖師失獅施濕詩尸虱十石拾時什食蝕實識史矢使屎駛始式示士世柿事拭誓逝勢是嗜噬適仕侍釋飾氏市恃室視試收手首守壽授售受瘦獸蔬樞梳殊抒輸叔舒淑疏書贖孰熟薯暑曙署蜀黍鼠屬術述樹束戍豎墅庶數漱恕刷耍摔衰甩帥栓拴霜雙爽誰水睡稅吮瞬順舜說碩朔爍斯撕嘶思私司絲死肆寺嗣四伺似飼巳鬆聳慫頌送宋訟誦搜艘擻嗽蘇酥俗素速粟僳塑溯宿訴肅酸蒜算雖隋隨綏髓碎歲穗遂隧祟孫損筍蓑梭唆縮瑣索鎖所塌他它她塔獺撻蹋踏胎苔抬台泰太態汰坍攤貪癱灘壇檀痰潭譚談坦毯袒碳探嘆炭湯塘搪堂棠膛唐糖倘躺淌趟燙掏濤滔絛萄桃逃淘陶討套特藤騰疼謄梯剔踢銻提題蹄啼體替嚏惕涕剃屜天添填田甜恬舔腆挑條迢眺跳貼鐵帖廳聽烴汀廷停亭庭挺艇通桐酮瞳同銅彤童桶捅筒統痛偷投頭透凸禿突圖徒途涂屠土吐兔湍團推頹腿蛻褪退吞屯臀拖托脫鴕陀馱駝橢妥拓唾挖哇蛙窪娃瓦襪歪外豌彎灣玩頑丸烷完碗挽晚皖惋宛婉萬腕汪王亡枉網往旺望忘妄威巍微危韋違桅圍唯惟為濰維葦萎委偉偽尾緯未蔚味畏胃喂魏位渭謂尉慰衛瘟溫蚊文聞紋吻穩紊問嗡翁瓮撾蝸渦窩我斡臥握沃巫嗚鎢烏污誣屋無蕪梧吾吳毋武五捂午舞伍侮塢戊霧晤物勿務悟誤昔熙析西硒矽晰嘻吸錫犧稀息希悉膝夕惜熄烯溪汐犀檄襲席習媳喜銑洗系隙戲細瞎蝦匣霞轄暇峽俠狹下廈夏嚇掀先仙鮮纖咸賢銜舷閑涎弦嫌顯險現獻縣腺餡羨憲陷限線相廂鑲香箱襄湘鄉翔祥詳想響享項巷橡像向象蕭硝霄削哮囂銷消宵淆曉小孝校肖嘯笑效楔些歇蠍鞋協挾攜邪斜脅諧寫械卸蟹懈泄瀉謝屑薪芯鋅欣辛新忻心信舋星腥猩惺興刑型形邢行醒幸杏性姓兄凶胸匈洶雄熊休修羞朽嗅鏽秀袖繡墟戌需虛噓須徐許蓄酗敘旭序畜恤絮婿緒續軒喧宣懸旋玄選癬眩絢靴薛學穴雪血勛熏循旬詢尋馴巡殉汛訓訊遜迅壓押鴉鴨呀丫芽牙蚜崖衙涯雅啞亞訝焉咽閹煙淹鹽嚴研蜒岩延言顏閻炎沿奄掩眼衍演艷堰燕厭硯雁唁彥焰宴諺驗殃央鴦秧楊揚佯瘍羊洋陽氧仰痒養樣漾邀腰妖瑤搖堯遙窯謠姚咬舀藥要耀椰噎耶爺野冶也頁掖業葉曳腋夜液一壹醫揖銥依伊衣頤夷遺移儀胰疑沂宜姨彝椅蟻倚已乙矣以藝抑易邑屹億役臆逸肄疫亦裔意毅憶義益溢詣議誼譯異翼翌繹茵蔭因殷音陰姻吟銀淫寅飲尹引隱印英櫻嬰鷹應纓瑩螢營熒蠅迎贏盈影穎硬映喲擁佣臃癰庸雍踴蛹詠泳涌永恿勇用幽優悠憂尤由郵鈾猶油游酉有友右佑釉誘又幼迂淤於盂榆虞愚輿余俞逾魚愉渝漁隅予娛雨與嶼禹宇語羽玉域芋郁吁遇喻峪御愈欲獄育譽浴寓裕預豫馭鴛淵冤元垣袁原援轅園員圓猿源緣遠苑願怨院曰約越躍鑰岳粵月悅閱耘雲鄖勻隕允運蘊醞暈韻孕匝砸雜栽哉災宰載再在咱攢暫贊贓臟葬遭糟鑿藻棗早澡蚤躁噪造皂灶燥責擇則澤賊怎增憎曾贈扎喳渣札軋鍘閘眨柵榨咋乍炸詐摘齋宅窄債寨瞻氈詹粘沾盞斬輾嶄展蘸棧佔戰站湛綻樟章彰漳張掌漲杖丈帳賬仗脹瘴障招昭找沼趙照罩兆肇召遮折哲蟄轍者鍺蔗這浙珍斟真甄砧臻貞針偵枕疹診震振鎮陣蒸掙睜征猙爭怔整拯正政幀症鄭証芝枝支吱蜘知肢脂汁之織職直植殖執值侄址指止趾隻旨紙志摯擲至致置幟峙制智秩稚質炙痔滯治窒中盅忠鐘衷終種腫重仲眾舟周州洲謅粥軸肘帚咒皺宙晝驟珠株蛛朱豬諸誅逐竹燭煮拄矚囑主著柱助蛀貯鑄筑住注祝駐抓爪拽專磚轉撰賺篆樁庄裝妝撞壯狀椎錐追贅墜綴諄准捉拙卓桌琢茁酌啄著灼濁茲咨資姿滋淄孜紫仔籽滓子自漬字鬃棕蹤宗綜總縱鄒走奏揍租足卒族祖詛阻組鑽纂嘴醉最罪尊遵昨左佐柞做作坐座亍丌兀丐廿卅丕亙丞鬲孬噩禺匕乇夭爻卮氐囟胤馗毓睪亟鼐乜乩亓羋孛嗇嘏仄厙厝厴厥靨贗匚叵匭匱匾賾卦卣刈刎剄刳劌剴剌剞剡剜蒯剽劂劁劓罔仃仉仂仨仡仞傴仳伢佤仵倀傖伉佇佞佧攸佚佝佟佗伽佶佴侑侉侃侏佾佻儕佼儂侔儔儼儷俅俚俁俜俑俟俸倩偌俳倬倏倭俾倜倌倥倨僨偃偕偈偎傯僂儻儐儺傺僖儆僭僬僦僮儇儋仝汆佘僉俎龠糴兮巽黌馘囅夔匍訇匐鳧夙兕兗亳袞袤褻臠裒稟嬴蠃羸冱冽冼塚冥訐訌訕謳詎訥詁訶詆詔詒誆誄詿詰詼詵詬詮諍諢詡誚誥誑誒諏諑諉諛諗諂誶諶諫謔謁諤諭諼諳諦諮諞謨讜謖謚謐謫譾譖譙譎讞譫讖巹阢阡阱阪阽阼陂陘陔陟隉陬陲陴隈隍隗隰邗邛鄺邙鄔邡邴邳邶鄴邸邰郟郅邾鄶郇鄆酈郢郜郗郛郫郯郾鄄鄢鄞鄣鄱鄯鄹酃酆芻奐勱劬劭劾哿勖勰叟燮矍鬯弁畚胇坌堊塾墼壅壑圩圬圳壙圮圯壢圻坩坫壚坼坻坨坭坶坳埡垤垌塏埏垓垠埕塒堝塤埒垸埴埸埤堋堍埽埭堀堞堙堠塥墁墉墀馨鼙懿艽艿芏芊芨芄芎芑薌芙芫芸芾芰藶苣芘芷芮莧萇蓯芩芴芡芟苧芤苡茉苤蘢茇苜苴苒茌苻苓蔦茆塋煢苠苕茜荑蕘蓽茈莒茼茴茱莛蕎茯荏荇荃薈荀茗薺茭茺茳犖滎茛藎蓀葒荸蒔萵莠莪莓蒞荼莩荽蕕荻莘莞莨鶯菁萁菥菘堇萋菝菽菖萸萑萆菔菟萏萃菸菹菪菅菀縈菰菡葑葚葙葳蕆葺蕢葸萼葆葩葶蔞蒎萱葭蓁蓍蓐驀蓓蓊蒿蒺蘺蒡蒹蒴蒗蕷蔌甍蓰蘞蔟藺蕖蔻蓿蓼蕙蕈蕨蕤蕞蕺瞢蕃蘄蕻薤薨薇薏蕹藪薜薅薹薷薰蘚藜藿蘧蘅蘩蘼廾弈夼奩耷奕奚奘匏尢尥尬尷捫摶抻拊拚拗拮撟拶挹捋捃掭揶捱捺掎摑捭掬掊捩掮摜揲揠撳揄揎摒揆掾攄摁搋搛搠榐搦搡摞攖摭撖摺擷擼撙攛擐擗擤擢攉攥攮弋忒弒叱嘰叩叨叻吒吆嘸囈呔嚦呃唄咂呷呱呤咚嚀咄呶呦咭哂噠咧咦嘵嗶呲噦咻咿噲嚌咩咪噥哏哞嘜哧嘮哽唔哳嗩唏唑唧唪嘖喏喵囀啁啕啐唷啖啵啶唳唰啜喋嗒喃喱喈喁喟啾嗖喑啻嗟嘍嚳喔喙嗷嗉嘟嗑囁嗔嗦嗝嗄嗯嗥嗲噯嗌嗍嗨嗤轡嘈嘌嘁嚶嗾嘀嘧噘嘹噗嘬噢噙嚕噌嚆噤噱噫嚅嚓囔囝囡圇囫囹囿圄圊圉圜幃帙帔帑幬幘幗帷幄幔幛幡岌屺岍岐嶇岈峴岑嵐岵岢岬岫岱岣岷嶧峒嶠峋崢嶗崍崧崦崮崤崞崆崛嶸崴崽嵬崳嵯嶁嵫嵋嵊嵩嶂嶙嶝豳嶷巔彳彷徂徇徉徠徙徜徨徭徵徼衢犰犴獷狃狁狎狒狨獪狩猻狴狷猁狳獫狺狻猗猓玀猊猞猝獼猢猥猱獐獍獗獠獬獯獾舛夥飧夤餳飩餼飪飫飭飴餉餑餘餛餿饃饈饉饌庀廡庋庖庥庠庹庵庾庳賡廒廑廛廨廩膺忉忖懺憮忮慪忡忤愾悵愴忪忭忸怙怵怦怛怏怍怩怫怊懌怡慟懨惻愷恂恪惲悖悚慳悝悃悒悌悛愜悻悱惝惘惆惚悴慍憒愕愣惴愀愎愫慊慵憬憔憧懍懵忝隳閂閆闈閎閔閌闥閭閫鬮閬閾閶閿閽閼闃闋闔闐闕闞戕汔汜汊灃沅沐沔沌汨汴汶沆溈泐泔沭瀧瀘泱泗泠泖濼泫泮沱泓泯涇洹洧洌浹湞洇洄洙洎洫澮洮洵洚瀏滸潯洳涑浯淶潿浞涓涔浠浼浣渚淇淅淞瀆涿淠澠淦淝淙瀋涫淥涮渫湮湎湫溲湟漵湓湔渲渥湄灩溱溘灄漭瀅溥溧溽溷潷溴滏溏滂溟潢瀠瀟漕滹漯漶瀲瀦漪漉漩澉澍澌潸潲潼潺瀨濉澧澹澶濂濡濮濠濯瀚瀣瀛瀹瀵灝灞宄宕宓宥宸甯騫搴寤寮褰寰蹇謇迓迕迥迮迤邇迦逕迨逅逄逋邐逑逍逖逡逵逶逭逯遄遑遒遐遨遘遢遛暹遴遽邂邈邃邋彗彖彘尻咫屐屙孱屣屨羼弳弩弭艴弼鬻妁妃妍嫵嫗妣妗姊媯妞妤姒妲妯姍妾婭嬈姝孌姣姘娌娉媧嫻娑娣娓婀婧婊婕娼婢嬋媼媛婷婺媾嫫媲嬡嬪媸嫠嫣嬙嫖嫦嫘嫜嬉嬗嬖嬲嬤孀尕孚孥孳孑孓孢駔駟駙騶驛駑駘驍驊駢驪騏騍騅驂騭騖驁騮騸驃驄驏驥驤紆紂紇紈纊紜紕紓紺紲紱縐紼絀絎絳綆綃綈綾綺緋緄綞綬綹綣綰緇緙緗緹緲繢緦緶緱縋緡縉縝縟縞縭縊縑繽縹縵縲繆繅纈繚繒繾繰繯纘畿甾邕玎璣瑋玢玟玨珂瓏玷玳珀珈珥珙頊琊珩珧珞璽琿璉琪瑛琦琥琨琰琮琬琛琚瑁瑜瑗瑕瑙璦瑭瑾璜瓔璀璁璇璋璞璨璩璐璧瓚璺韙韞韜杌杓杞杈榪櫪枇杪杳枘杵棖樅梟枋杷杼柰櫛柘櫳柩枰櫨柙枵柚枳柝梔柃枸柢櫟柁檉栲栳椏橈桎楨桄榿梃栝樺桁檜桀欒桉栩梵梏桴桷梓桫櫺楮棼櫝槧棹欏棰椋槨楗棣椐楱椹楠楂楝欖楫楸椴槌櫬櫚槎櫸楦楣楹榛榧榻榫榭槔榱槁槊檳榕櫧榍槿檣槭樗樘櫫槲橄樾檠橐橛樵檎櫓樽樨橘櫞檑檐檁檗猷歿殂殤殄殞殮殍殫殛殯殪軔軛軻轤軹軼軫轢軺軾輊輇輅輒輦輞輟輜輳轆轔戔戧戛戟戢戡戥戤戩臧甌瓴瓿甏甑甓旮旯旰昊曇杲昃昕昀炅曷昝昴昱昶昵耆晟曄晁晏暉晡晷暄暌曖暝暾曛曜曦曩賁貰貺貽贄貲賅贐賑賚賕賧賻覘覬覡覿覦覯覲覷牮牝牯牾牿犄犋犍犒挈挲掰搿擘耄毳毽毿毹氅氌氆氍氕氘氙氚氡氬氤氪氳敕牘牒牖爰虢刖肜肓朊肱肫肭肴朧臚胛胂冑胙胍胗朐胝脛胱胴胭膾胼朕豚腡脞脬脘腌腓腴腱腠腩膃塍媵膈膂臏滕膣臌朦臊膻膦歟欷欹歃歆歙颮颯颶颼飆彀轂觳斐齏斕於旆旄旃旌旎旒旖煬煒燉炷炫炱燁烊焐焓燜焯焱煜煨煲煸熳熵熨熠燠燔燧燹爝爨燾煦熹戾戽扃扈扉祀祆祉祛祜祓祚檷祗祠禎祧祺禪禊禚禧禳忑忐懟恝恚恧恁恙恣愨愆愍慝憩憝懋懣戇聿沓澩淼磯矸碭砉硨砑斫砭砝礪礱砟砥砬砣砩硎硭硤磽砦硐硌磧碓碚碇磣碡碣碲碥磔磉磬磲礅磴礓礤礞礡龕黹黻黼盱眄盹眇眈眚眢眙眭眵眸睞瞼睇睚睨睢睥睿瞍睽瞀瞌瞑瞟瞠瞰瞵瞽町畀畎畋畈畛畬畹罘罡罟詈罨羆罹羈罾盍盥蠲釓釔釙釗釕釷釧釤鍆釵釹鈦鉅鈑鈐鈁鈧鈄鈥鈀鈺鉦鈷鈳鈽鈸鉞鉬鉭鈿鑠鈰鉉鉈鉍鈮鈹鐸銬銠鉺銪鋮鋏鐃鐺銦鎧銖鋌銩鏵銓鉿鎩銚錚銫銃鐋銨銣鐒錸鋱鏗鋰鋯鋨銼鋝鋃鋟鋦錒錆錛錁錕錮錈錟錙鍥鍇鍶鍔鍤鎪鍰鏤鏘鏌鎘鐫鎦鎰鎵鑌鏢鏜鏝鏍鏞鏃鏇鏑鐔鏷鐓鑭鐠鏹鐙鑊鐲鐿鑣鍾矧矬雉秕秭秣秫嵇稃稂稞稔稹稷穡黏馥穰皈皎皓皙皤瓞瓠甬鳩鳶鴇鴆鴣鶇鸕鴝鴟鷥鴯鷙鴰鵂鸞鵓鸝鵠鵒鷴鵜鵡鵪鵯鶉鶘鶚鷂鶼鸚鷓鷚鷯鷦鷲鷸鷺鸛疔癤癘疝疣疳疸疰痂痍痣癆痦痤癇痧瘃痱痼痿瘐瘀癉瘌瘞瘊瘥瘕瘙瘼瘢瘠瘭瘰癭瘵癃癮瘳癩癜癖癲翊竦穸穹窀窆窈窕竇窠窬窨窶窳衩衲衽衿袂袢襠袷袼裉褳裎襝襉裱褚裼裨裾裰褡褙褓褸褊襤褫褶襁襦襻疋胥皸皴矜耒耔耖耜耦耬耩耨耋耵聃聆聹聒聵聱覃頇頎頏頡頜頦頷顎顓顳顢顙顥顰虔虯蟣蠆虺虼虻蚨蚍蚋蜆蚝蚧蚣蚪蚓蚩蚶蛄蚵蠣蚰蚺蚱蚯蛉蟶蚴蛩蛺蟯蛭螄蛐蜓蛞蠐蛟蛘蛑蜃蜇蛸蜈蜊蜍蜉蜣蜻蜞蜥蜮蜚蜾蟈蜴蜱蜩蜷蜿螂蜢蠑蝻蝠蝌蝮蝓蝣螻蝤蝙蝥螓螯蟒蟆螈螅螭螗螃螫蟥螬螵螳蟋蟓螽蟑蟀蟊蟛蟪蟠蠖蠓蟾蠊蠛蠡蠹蠷缶罌罄罅舐竺竽笈篤笄筧笊笫笏筇笸笪笙笮笱笠笥笤笳籩笞筘篳筅筵筌箏筠筮筲筱箐簀篋箸箬箝籜箅簞箜簫箴簣篁篌篝篚篥篦篪簌篾簏籪簋簟簪簦簸籟籀臾舁舂舄臬衄舡舢艤舯舨舫舸艫舳舴艄艉艋艏艚艟艨衾裊袈裘裟襞羝羥羧羯羰羲敉粑粞粢粲粼粽糝糌糈糅糗糨艮暨羿翎翕翥翡翦翩翮翳糸縶綦綮繇纛麩麴赳趄趔趑趲赧赭豇豉酊酐酎酏酤酢酡酩酯釅釃酲酴酹醅醐醍醑醢醣醪醭醮醯醵醴醺豕鹺躉跫踅蹙蹩趵趿趼趺蹌跖跗跚躒跎跏跛跆跬蹺蹕跣躚躋跤踉跽踔踝踟躓踮踣躑蹀踵踽踱蹉蹁蹂躡蹣蹊躕蹶蹼蹯蹴躅躪躔躐躦躞豸貂貊貅貘貔斛觖觴觚觜觥觫觶訾謦靚雩靂雯霆霽霈霏霎霪靄霰霾齔齟齙齠齜齦齬齪齷黽黿鼉隹隼雋雎雒瞿讎銎鑾鋈鏨鍪鏊鎏鑫魷魴鱍鱸穌鮒鱟鮐鮚鮪鮞鱭鮫鯗鱘鯁鱺鰱鰹鰣鰷鯀鯊鯇鯽鯖鯪鯫鯡鯤鯧鯢鯰鯛鯔鰈鱷鰍鰒鰉鰲鰭鰨鰥鰩鰳鰾鱈鰻鱖鱔鱒鱧靼鞅韃鞔韉鞫鞣骱骰骷鶻骼髁髀髏髂髖髕髑魅魃魘魎魈魍魑饗饜餮饕饔髟髡髦髯髫髻髭髹鬈鬢鬟鬣麼麾縻麂麇麈麋麒鏖麝麟黛黜黝黠黟黷黧黥黲黯鼢鼬鼯鼴鼷鼽鼾";

	/**
	 * 简体字char对比,可修改为保存在UTF-8格式外部文件读入，自定义mapping
	 */
	public static String turnSimpe = "啊阿埃挨哎唉哀皑癌蔼矮艾碍爱隘鞍氨安俺按暗岸胺案肮昂盎凹敖熬翱袄傲奥懊澳芭捌扒叭吧笆八疤巴拔跋靶把耙坝霸罢爸白柏百摆佰败拜稗斑班搬扳般颁板版扮拌伴瓣半办绊邦帮梆榜膀绑棒磅蚌镑傍谤苞胞包褒剥薄雹保堡饱宝抱报暴豹鲍爆杯碑悲卑北辈背贝钡倍狈备惫焙被奔苯本笨崩绷甭泵蹦迸逼鼻比鄙笔彼碧蓖蔽毕毙毖币庇痹闭敝弊必辟壁臂避陛鞭边编贬扁便变卞辨辩辫遍标彪膘表鳖憋别瘪彬斌濒滨宾摈兵冰柄丙秉饼炳病并玻菠播拨钵波博勃搏铂箔伯帛舶脖膊渤泊驳捕卜哺补埠不布步簿部怖擦猜裁材才财睬踩采彩菜蔡餐参蚕残惭惨灿苍舱仓沧藏操糙槽曹草厕策侧册测层蹭插叉茬茶查碴搽察岔差诧拆柴豺搀掺蝉馋谗缠铲产阐颤昌猖场尝常长偿肠厂敞畅唱倡超抄钞朝嘲潮巢吵炒车扯撤掣彻澈郴臣辰尘晨忱沉陈趁衬撑称城橙成呈乘程惩澄诚承逞骋秤吃痴持匙池迟弛驰耻齿侈尺赤翅斥炽充冲虫崇宠抽酬畴踌稠愁筹仇绸瞅丑臭初出橱厨躇锄雏滁除楚础储矗搐触处揣川穿椽传船喘串疮窗幢床闯创吹炊捶锤垂春椿醇唇淳纯蠢戳绰疵茨磁雌辞慈瓷词此刺赐次聪葱囱匆从丛凑粗醋簇促蹿篡窜摧崔催脆瘁粹淬翠村存寸磋撮搓措挫错搭达答瘩打大呆歹傣戴带殆代贷袋待逮怠耽担丹单郸掸胆旦氮但惮淡诞弹蛋当挡党荡档刀捣蹈倒岛祷导到稻悼道盗德得的蹬灯登等瞪凳邓堤低滴迪敌笛狄涤翟嫡抵底地蒂第帝弟递缔颠掂滇碘点典靛垫电佃甸店惦奠淀殿碉叼雕凋刁掉吊钓调跌爹碟蝶迭谍叠丁盯叮钉顶鼎锭定订丢东冬董懂动栋侗恫冻洞兜抖斗陡豆逗痘都督毒犊独读堵睹赌杜镀肚度渡妒端短锻段断缎堆兑队对墩吨蹲敦顿囤钝盾遁掇哆多夺垛躲朵跺舵剁惰堕蛾峨鹅俄额讹娥恶厄扼遏鄂饿恩而儿耳尔饵洱二贰发罚筏伐乏阀法珐藩帆番翻樊矾钒繁凡烦反返范贩犯饭泛坊芳方肪房防妨仿访纺放菲非啡飞肥匪诽吠肺废沸费芬酚吩氛分纷坟焚汾粉奋份忿愤粪丰封枫蜂峰锋风疯烽逢冯缝讽奉凤佛否夫敷肤孵扶拂辐幅氟符伏俘服浮涪福袱弗甫抚辅俯釜斧脯腑府腐赴副覆赋复傅付阜父腹负富讣附妇缚咐噶嘎该改概钙盖溉干甘杆柑竿肝赶感秆敢赣冈刚钢缸肛纲岗港杠篙皋高膏羔糕搞镐稿告哥歌搁戈鸽胳疙割革葛格蛤阁隔铬个各给根跟耕更庚羹埂耿梗工攻功恭龚供躬公宫弓巩汞拱贡共钩勾沟苟狗垢构购够辜菇咕箍估沽孤姑鼓古蛊骨谷股故顾固雇刮瓜剐寡挂褂乖拐怪棺关官冠观管馆罐惯灌贯光广逛瑰规圭硅归龟闺轨鬼诡癸桂柜跪贵刽辊滚棍锅郭国果裹过哈骸孩海氦亥害骇酣憨邯韩含涵寒函喊罕翰撼捍旱憾悍焊汗汉夯杭航壕嚎豪毫郝好耗号浩呵喝荷菏核禾和何合盒貉阂河涸赫褐鹤贺嘿黑痕很狠恨哼亨横衡恒轰哄烘虹鸿洪宏弘红喉侯猴吼厚候后呼乎忽瑚壶葫胡蝴狐糊湖弧虎唬护互沪户花哗华猾滑画划化话槐徊怀淮坏欢环桓还缓换患唤痪豢焕涣宦幻荒慌黄磺蝗簧皇凰惶煌晃幌恍谎灰挥辉徽恢蛔回毁悔慧卉惠晦贿秽会烩汇讳诲绘荤昏婚魂浑混豁活伙火获或惑霍货祸击圾基机畸稽积箕肌饥迹激讥鸡姬绩缉吉极棘辑籍集及急疾汲即嫉级挤几脊己蓟技冀季伎祭剂悸济寄寂计记既忌际妓继纪嘉枷夹佳家加荚颊贾甲钾假稼价架驾嫁歼监坚尖笺间煎兼肩艰奸缄茧检柬碱硷拣捡简俭剪减荐槛鉴践贱见键箭件健舰剑饯渐溅涧建僵姜将浆江疆蒋桨奖讲匠酱降蕉椒礁焦胶交郊浇骄娇嚼搅铰矫侥脚狡角饺缴绞剿教酵轿较叫窖揭接皆秸街阶截劫节桔杰捷睫竭洁结解姐戒藉芥界借介疥诫届巾筋斤金今津襟紧锦仅谨进靳晋禁近烬浸尽劲荆兢茎睛晶鲸京惊精粳经井警景颈静境敬镜径痉靖竟竞净炯窘揪究纠玖韭久灸九酒厩救旧臼舅咎就疚鞠拘狙疽居驹菊局咀矩举沮聚拒据巨具距踞锯俱句惧炬剧捐鹃娟倦眷卷绢撅攫抉掘倔爵觉决诀绝均菌钧军君峻俊竣浚郡骏喀咖卡咯开揩楷凯慨刊堪勘坎砍看康慷糠扛抗亢炕考拷烤靠坷苛柯棵磕颗科壳咳可渴克刻客课肯啃垦恳坑吭空恐孔控抠口扣寇枯哭窟苦酷库裤夸垮挎跨胯块筷侩快宽款匡筐狂框矿眶旷况亏盔岿窥葵奎魁傀馈愧溃坤昆捆困括扩廓阔垃拉喇蜡腊辣啦莱来赖蓝婪栏拦篮阑兰澜谰揽览懒缆烂滥琅榔狼廊郎朗浪捞劳牢老佬姥酪烙涝勒乐雷镭蕾磊累儡垒擂肋类泪棱楞冷厘梨犁黎篱狸离漓理李里鲤礼莉荔吏栗丽厉励砾历利例俐痢立粒沥隶力璃哩俩联莲连镰廉怜涟帘敛脸链恋炼练粮凉梁粱良两辆量晾亮谅撩聊僚疗燎寥辽潦了撂镣廖料列裂烈劣猎琳林磷霖临邻鳞淋凛赁吝拎玲菱零龄铃伶羚凌灵陵岭领另令溜琉榴硫馏留刘瘤流柳六龙聋咙笼窿隆垄拢陇楼娄搂篓漏陋芦卢颅庐炉掳卤虏鲁麓碌露路赂鹿潞禄录陆戮驴吕铝侣旅履屡缕虑氯律率滤绿峦挛孪滦卵乱掠略抡轮伦仑沦纶论萝螺罗逻锣箩骡裸落洛骆络妈麻玛码蚂马骂嘛吗埋买麦卖迈脉瞒馒蛮满蔓曼慢漫谩芒茫盲氓忙莽猫茅锚毛矛铆卯茂冒帽貌贸么玫枚梅霉煤没眉媒镁每美昧寐妹媚门闷们萌蒙檬盟锰猛梦孟眯醚靡糜迷谜弥米秘觅泌蜜密幂棉眠绵冕免勉娩缅面苗描瞄藐秒渺庙妙蔑灭民抿皿敏悯闽明螟鸣铭名命谬摸摹蘑模膜磨摩魔抹末莫墨默沫漠寞陌谋牟某拇牡亩姆母墓暮幕募慕木目睦牧穆拿哪呐钠那娜纳氖乃奶耐奈南男难囊挠脑恼闹淖呢馁内嫩能妮霓倪泥尼拟你匿腻逆溺蔫拈年碾撵捻念娘酿鸟尿捏聂孽啮镊镍涅您柠狞凝宁拧泞牛扭钮纽脓浓农弄奴努怒女暖虐疟挪懦糯诺哦欧鸥殴藕呕偶沤啪趴爬帕怕琶拍排牌徘湃派攀潘盘磐盼畔判叛乓庞旁耪胖抛咆刨炮袍跑泡呸胚培裴赔陪配佩沛喷盆砰抨烹澎彭蓬棚硼篷膨朋鹏捧碰坯砒霹批披劈琵毗啤脾疲皮匹痞僻屁譬篇偏片骗飘漂瓢票撇瞥拼频贫品聘乒坪苹萍平凭瓶评屏坡泼颇婆破魄迫粕剖扑铺仆莆葡菩蒲埔朴圃普浦谱曝瀑期欺栖戚妻七凄漆柒沏其棋奇歧畦崎脐齐旗祈祁骑起岂乞企启契砌器气迄弃汽泣讫掐恰洽牵扦铅千迁签仟谦乾黔钱钳前潜遣浅谴堑嵌欠歉枪呛腔羌墙蔷强抢橇锹敲悄桥瞧乔侨巧鞘撬翘峭俏窍切茄且怯窃钦侵亲秦琴勤芹擒禽寝沁青轻氢倾卿清擎晴氰情顷请庆琼穷秋丘邱球求囚酋泅趋区蛆曲躯屈驱渠取娶龋趣去圈颧权醛泉全痊拳犬券劝缺炔瘸却鹊榷确雀裙群然燃冉染瓤壤攘嚷让饶扰绕惹热壬仁人忍韧任认刃妊纫扔仍日戎茸蓉荣融熔溶容绒冗揉柔肉茹蠕儒孺如辱乳汝入褥软阮蕊瑞锐闰润若弱撒洒萨腮鳃塞赛叁三伞散桑嗓丧搔骚扫嫂瑟色涩森僧莎砂杀刹沙纱傻啥煞筛晒珊苫杉山删煽衫闪陕擅赡膳善汕扇缮伤商赏晌上尚裳梢捎稍烧芍勺韶少哨邵绍奢赊蛇舌舍赦摄射慑涉社设砷申呻伸身深娠绅神沈审婶甚肾慎渗声生甥牲升绳省盛剩胜圣师失狮施湿诗尸虱十石拾时什食蚀实识史矢使屎驶始式示士世柿事拭誓逝势是嗜噬适仕侍释饰氏市恃室视试收手首守寿授售受瘦兽蔬枢梳殊抒输叔舒淑疏书赎孰熟薯暑曙署蜀黍鼠属术述树束戍竖墅庶数漱恕刷耍摔衰甩帅栓拴霜双爽谁水睡税吮瞬顺舜说硕朔烁斯撕嘶思私司丝死肆寺嗣四伺似饲巳松耸怂颂送宋讼诵搜艘擞嗽苏酥俗素速粟僳塑溯宿诉肃酸蒜算虽隋随绥髓碎岁穗遂隧祟孙损笋蓑梭唆缩琐索锁所塌他它她塔獭挞蹋踏胎苔抬台泰太态汰坍摊贪瘫滩坛檀痰潭谭谈坦毯袒碳探叹炭汤塘搪堂棠膛唐糖倘躺淌趟烫掏涛滔绦萄桃逃淘陶讨套特藤腾疼誊梯剔踢锑提题蹄啼体替嚏惕涕剃屉天添填田甜恬舔腆挑条迢眺跳贴铁帖厅听烃汀廷停亭庭挺艇通桐酮瞳同铜彤童桶捅筒统痛偷投头透凸秃突图徒途涂屠土吐兔湍团推颓腿蜕褪退吞屯臀拖托脱鸵陀驮驼椭妥拓唾挖哇蛙洼娃瓦袜歪外豌弯湾玩顽丸烷完碗挽晚皖惋宛婉万腕汪王亡枉网往旺望忘妄威巍微危韦违桅围唯惟为潍维苇萎委伟伪尾纬未蔚味畏胃喂魏位渭谓尉慰卫瘟温蚊文闻纹吻稳紊问嗡翁瓮挝蜗涡窝我斡卧握沃巫呜钨乌污诬屋无芜梧吾吴毋武五捂午舞伍侮坞戊雾晤物勿务悟误昔熙析西硒矽晰嘻吸锡牺稀息希悉膝夕惜熄烯溪汐犀檄袭席习媳喜铣洗系隙戏细瞎虾匣霞辖暇峡侠狭下厦夏吓掀先仙鲜纤咸贤衔舷闲涎弦嫌显险现献县腺馅羡宪陷限线相厢镶香箱襄湘乡翔祥详想响享项巷橡像向象萧硝霄削哮嚣销消宵淆晓小孝校肖啸笑效楔些歇蝎鞋协挟携邪斜胁谐写械卸蟹懈泄泻谢屑薪芯锌欣辛新忻心信衅星腥猩惺兴刑型形邢行醒幸杏性姓兄凶胸匈汹雄熊休修羞朽嗅锈秀袖绣墟戌需虚嘘须徐许蓄酗叙旭序畜恤絮婿绪续轩喧宣悬旋玄选癣眩绚靴薛学穴雪血勋熏循旬询寻驯巡殉汛训讯逊迅压押鸦鸭呀丫芽牙蚜崖衙涯雅哑亚讶焉咽阉烟淹盐严研蜒岩延言颜阎炎沿奄掩眼衍演艳堰燕厌砚雁唁彦焰宴谚验殃央鸯秧杨扬佯疡羊洋阳氧仰痒养样漾邀腰妖瑶摇尧遥窑谣姚咬舀药要耀椰噎耶爷野冶也页掖业叶曳腋夜液一壹医揖铱依伊衣颐夷遗移仪胰疑沂宜姨彝椅蚁倚已乙矣以艺抑易邑屹亿役臆逸肄疫亦裔意毅忆义益溢诣议谊译异翼翌绎茵荫因殷音阴姻吟银淫寅饮尹引隐印英樱婴鹰应缨莹萤营荧蝇迎赢盈影颖硬映哟拥佣臃痈庸雍踊蛹咏泳涌永恿勇用幽优悠忧尤由邮铀犹油游酉有友右佑釉诱又幼迂淤于盂榆虞愚舆余俞逾鱼愉渝渔隅予娱雨与屿禹宇语羽玉域芋郁吁遇喻峪御愈欲狱育誉浴寓裕预豫驭鸳渊冤元垣袁原援辕园员圆猿源缘远苑愿怨院曰约越跃钥岳粤月悦阅耘云郧匀陨允运蕴酝晕韵孕匝砸杂栽哉灾宰载再在咱攒暂赞赃脏葬遭糟凿藻枣早澡蚤躁噪造皂灶燥责择则泽贼怎增憎曾赠扎喳渣札轧铡闸眨栅榨咋乍炸诈摘斋宅窄债寨瞻毡詹粘沾盏斩辗崭展蘸栈占战站湛绽樟章彰漳张掌涨杖丈帐账仗胀瘴障招昭找沼赵照罩兆肇召遮折哲蛰辙者锗蔗这浙珍斟真甄砧臻贞针侦枕疹诊震振镇阵蒸挣睁征狰争怔整拯正政帧症郑证芝枝支吱蜘知肢脂汁之织职直植殖执值侄址指止趾只旨纸志挚掷至致置帜峙制智秩稚质炙痔滞治窒中盅忠钟衷终种肿重仲众舟周州洲诌粥轴肘帚咒皱宙昼骤珠株蛛朱猪诸诛逐竹烛煮拄瞩嘱主著柱助蛀贮铸筑住注祝驻抓爪拽专砖转撰赚篆桩庄装妆撞壮状椎锥追赘坠缀谆准捉拙卓桌琢茁酌啄着灼浊兹咨资姿滋淄孜紫仔籽滓子自渍字鬃棕踪宗综总纵邹走奏揍租足卒族祖诅阻组钻纂嘴醉最罪尊遵昨左佐柞做作坐座亍丌兀丐廿卅丕亘丞鬲孬噩禺匕乇夭爻卮氐囟胤馗毓睾亟鼐乜乩亓芈孛啬嘏仄厍厝厣厥靥赝匚叵匦匮匾赜卦卣刈刎刭刳刿剀剌剞剡剜蒯剽劂劁劓罔仃仉仂仨仡仞伛仳伢佤仵伥伧伉伫佞佧攸佚佝佟佗伽佶佴侑侉侃侏佾佻侪佼侬侔俦俨俪俅俚俣俜俑俟俸倩偌俳倬倏倭俾倜倌倥倨偾偃偕偈偎偬偻傥傧傩傺僖儆僭僬僦僮儇儋仝氽佘佥俎龠籴兮巽黉馘冁夔匍訇匐凫夙兕兖亳衮袤亵脔裒禀嬴蠃羸冱冽冼冢冥讦讧讪讴讵讷诂诃诋诏诒诓诔诖诘诙诜诟诠诤诨诩诮诰诳诶诹诼诿谀谂谄谇谌谏谑谒谔谕谖谙谛谘谝谟谠谡谥谧谪谫谮谯谲谳谵谶卺阢阡阱阪阽阼陂陉陔陟陧陬陲陴隈隍隗隰邗邛邝邙邬邡邴邳邶邺邸邰郏郅邾郐郇郓郦郢郜郗郛郫郯郾鄄鄢鄞鄣鄱鄯鄹酃酆刍奂劢劬劭劾哿勖勰叟燮矍鬯弁畚巯坌垩塾墼壅壑圩圬圳圹圮圯坜圻坩坫垆坼坻坨坭坶坳垭垤垌垲埏垓垠埕埘埚埙埒垸埴埸埤堋堍埽埭堀堞堙堠塥墁墉墀馨鼙懿艽艿芏芊芨芄芎芑芗芙芫芸芾芰苈苣芘芷芮苋苌苁芩芴芡芟苎芤苡茉苤茏茇苜苴苒茌苻苓茑茆茔茕苠苕茜荑荛荜茈莒茼茴茱莛荞茯荏荇荃荟荀茗荠茭茺茳荦荥茛荩荪荭荸莳莴莠莪莓莅荼莩荽莸荻莘莞莨莺菁萁菥菘堇萋菝菽菖萸萑萆菔菟萏萃菸菹菪菅菀萦菰菡葑葚葙葳蒇葺蒉葸萼葆葩葶蒌蒎萱葭蓁蓍蓐蓦蓓蓊蒿蒺蓠蒡蒹蒴蒗蓣蔌甍蓰蔹蔟蔺蕖蔻蓿蓼蕙蕈蕨蕤蕞蕺瞢蕃蕲蕻薤薨薇薏蕹薮薜薅薹薷薰藓藜藿蘧蘅蘩蘼廾弈夼奁耷奕奚奘匏尢尥尬尴扪抟抻拊拚拗拮挢拶挹捋捃掭揶捱捺掎掴捭掬掊捩掮掼揲揠揿揄揎摒揆掾摅摁搋搛搠搌搦搡摞撄摭撖摺撷撸撙撺擐擗擤擢攉攥攮弋忒弑叱叽叩叨叻吒吆呒呓呔呖呃呗咂呷呱呤咚咛咄呶呦咭哂哒咧咦哓哔呲哕咻咿哙哜咩咪哝哏哞唛哧唠哽唔哳唢唏唑唧唪啧喏喵啭啁啕啐唷啖啵啶唳唰啜喋嗒喃喱喈喁喟啾嗖喑啻嗟喽喾喔喙嗷嗉嘟嗑嗫嗔嗦嗝嗄嗯嗥嗲嗳嗌嗍嗨嗤辔嘈嘌嘁嘤嗾嘀嘧噘嘹噗嘬噢噙噜噌嚆噤噱噫嚅嚓囔囝囡囵囫囹囿圄圊圉圜帏帙帔帑帱帻帼帷幄幔幛幡岌屺岍岐岖岈岘岑岚岵岢岬岫岱岣岷峄峒峤峋峥崂崃崧崦崮崤崞崆崛嵘崴崽嵬嵛嵯嵝嵫嵋嵊嵩嶂嶙嶝豳嶷巅彳彷徂徇徉徕徙徜徨徭徵徼衢犰犴犷狃狁狎狒狨狯狩狲狴狷猁狳猃狺狻猗猓猡猊猞猝猕猢猥猱獐獍獗獠獬獯獾舛夥飧夤饧饨饩饪饫饬饴饷饽馀馄馊馍馐馑馔庀庑庋庖庥庠庹庵庾庳赓廒廑廛廨廪膺忉忖忏怃忮怄忡忤忾怅怆忪忭忸怙怵怦怛怏怍怩怫怊怿怡恸恹恻恺恂恪恽悖悚悭悝悃悒悌悛惬悻悱惝惘惆惚悴愠愦愕愣惴愀愎愫慊慵憬憔憧懔懵忝隳闩闫闱闳闵闶闼闾阃阄阆阈阊阌阍阏阒阕阖阗阙阚戕汔汜汊沣沅沐沔沌汨汴汶沆沩泐泔沭泷泸泱泗泠泖泺泫泮沱泓泯泾洹洧洌浃浈洇洄洙洎洫浍洮洵洚浏浒浔洳涑浯涞涠浞涓涔浠浼浣渚淇淅淞渎涿淠渑淦淝淙渖涫渌涮渫湮湎湫溲湟溆湓湔渲渥湄滟溱溘滠漭滢溥溧溽溷滗溴滏溏滂溟潢潆潇漕滹漯漶潋潴漪漉漩澉澍澌潸潲潼潺濑濉澧澹澶濂濡濮濠濯瀚瀣瀛瀹瀵灏灞宄宕宓宥宸甯骞搴寤寮褰寰蹇謇迓迕迥迮迤迩迦迳迨逅逄逋逦逑逍逖逡逵逶逭逯遄遑遒遐遨遘遢遛暹遴遽邂邈邃邋彗彖彘尻咫屐屙孱屣屦羼弪弩弭艴弼鬻妁妃妍妩妪妣妗姊妫妞妤姒妲妯姗妾娅娆姝娈姣姘娌娉娲娴娑娣娓婀婧婊婕娼婢婵媪媛婷婺媾嫫媲嫒嫔媸嫠嫣嫱嫖嫦嫘嫜嬉嬗嬖嬲嬷孀尕孚孥孳孑孓孢驵驷驸驺驿驽骀骁骅骈骊骐骒骓骖骘骛骜骝骟骠骢骣骥骧纡纣纥纨纩纭纰纾绀绁绂绉绋绌绗绛绠绡绨绫绮绯绲缍绶绺绻绾缁缂缃缇缈缋缌缏缑缒缗缙缜缛缟缡缢缣缤缥缦缧缪缫缬缭缯缱缲缳缵畿甾邕玎玑玮玢玟珏珂珑玷玳珀珈珥珙顼琊珩珧珞玺珲琏琪瑛琦琥琨琰琮琬琛琚瑁瑜瑗瑕瑙瑷瑭瑾璜璎璀璁璇璋璞璨璩璐璧瓒璺韪韫韬杌杓杞杈杩枥枇杪杳枘杵枨枞枭枋杷杼柰栉柘栊柩枰栌柙枵柚枳柝栀柃枸柢栎柁柽栲栳桠桡桎桢桄桤梃栝桦桁桧桀栾桉栩梵梏桴桷梓桫棂楮棼椟椠棹椤棰椋椁楗棣椐楱椹楠楂楝榄楫楸椴槌榇榈槎榉楦楣楹榛榧榻榫榭槔榱槁槊槟榕槠榍槿樯槭樗樘橥槲橄樾檠橐橛樵檎橹樽樨橘橼檑檐檩檗猷殁殂殇殄殒殓殍殚殛殡殪轫轭轲轳轵轶轸轹轺轼轾辁辂辄辇辋辍辎辏辘辚戋戗戛戟戢戡戥戤戬臧瓯瓴瓿甏甑甓旮旯旰昊昙杲昃昕昀炅曷昝昴昱昶昵耆晟晔晁晏晖晡晷暄暌暧暝暾曛曜曦曩贲贳贶贻贽赀赅赆赈赉赇赕赙觇觊觋觌觎觏觐觑牮牝牯牾牿犄犋犍犒挈挲掰搿擘耄毳毽毵毹氅氇氆氍氕氘氙氚氡氩氤氪氲敕牍牒牖爰虢刖肜肓朊肱肫肭肴胧胪胛胂胄胙胍胗朐胝胫胱胴胭脍胼朕豚脶脞脬脘腌腓腴腱腠腩腽塍媵膈膂膑滕膣臌朦臊膻膦欤欷欹歃歆歙飑飒飓飕飙彀毂觳斐齑斓於旆旄旃旌旎旒旖炀炜炖炷炫炱烨烊焐焓焖焯焱煜煨煲煸熳熵熨熠燠燔燧燹爝爨焘煦熹戾戽扃扈扉祀祆祉祛祜祓祚祢祗祠祯祧祺禅禊禚禧禳忑忐怼恝恚恧恁恙恣悫愆愍慝憩憝懋懑戆聿沓泶淼矶矸砀砉砗砑斫砭砝砺砻砟砥砬砣砩硎硭硖硗砦硐硌碛碓碚碇碜碡碣碲碥磔磉磬磲礅磴礓礤礞礴龛黹黻黼盱眄盹眇眈眚眢眙眭眵眸睐睑睇睚睨睢睥睿瞍睽瞀瞌瞑瞟瞠瞰瞵瞽町畀畎畋畈畛畲畹罘罡罟詈罨罴罹羁罾盍盥蠲钆钇钋钊钌钍钏钐钔钗钕钛钜钣钤钫钪钭钬钯钰钲钴钶钸钹钺钼钽钿铄铈铉铊铋铌铍铎铐铑铒铕铖铗铙铛铟铠铢铤铥铧铨铪铩铫铮铯铳铴铵铷铹铼铽铿锂锆锇锉锊锒锓锔锕锖锛锞锟锢锩锬锱锲锴锶锷锸锼锾镂锵镆镉镌镏镒镓镔镖镗镘镙镛镞镟镝镡镤镦镧镨镪镫镬镯镱镳锺矧矬雉秕秭秣秫嵇稃稂稞稔稹稷穑黏馥穰皈皎皓皙皤瓞瓠甬鸠鸢鸨鸩鸪鸫鸬鸲鸱鸶鸸鸷鸹鸺鸾鹁鹂鹄鹆鹇鹈鹉鹌鹎鹑鹕鹗鹞鹣鹦鹧鹨鹩鹪鹫鹬鹭鹳疔疖疠疝疣疳疸疰痂痍痣痨痦痤痫痧瘃痱痼痿瘐瘀瘅瘌瘗瘊瘥瘕瘙瘼瘢瘠瘭瘰瘿瘵癃瘾瘳癞癜癖癫翊竦穸穹窀窆窈窕窦窠窬窨窭窳衩衲衽衿袂袢裆袷袼裉裢裎裣裥裱褚裼裨裾裰褡褙褓褛褊褴褫褶襁襦襻疋胥皲皴矜耒耔耖耜耦耧耩耨耋耵聃聆聍聒聩聱覃顸颀颃颉颌颏颔颚颛颞颟颡颢颦虔虬虮虿虺虼虻蚨蚍蚋蚬蚝蚧蚣蚪蚓蚩蚶蛄蚵蛎蚰蚺蚱蚯蛉蛏蚴蛩蛱蛲蛭蛳蛐蜓蛞蛴蛟蛘蛑蜃蜇蛸蜈蜊蜍蜉蜣蜻蜞蜥蜮蜚蜾蝈蜴蜱蜩蜷蜿螂蜢蝾蝻蝠蝌蝮蝓蝣蝼蝤蝙蝥螓螯蟒蟆螈螅螭螗螃螫蟥螬螵螳蟋蟓螽蟑蟀蟊蟛蟪蟠蠖蠓蟾蠊蠛蠡蠹蠼缶罂罄罅舐竺竽笈笃笄笕笊笫笏筇笸笪笙笮笱笠笥笤笳笾笞筘筚筅筵筌筝筠筮筲筱箐箦箧箸箬箝箨箅箪箜箫箴篑篁篌篝篚篥篦篪簌篾簏簖簋簟簪簦簸籁籀臾舁舂舄臬衄舡舢舣舯舨舫舸舻舳舴艄艉艋艏艚艟艨衾袅袈裘裟襞羝羟羧羯羰羲敉粑粞粢粲粼粽糁糌糈糅糗糨艮暨羿翎翕翥翡翦翩翮翳糸絷綦綮繇纛麸麴赳趄趔趑趱赧赭豇豉酊酐酎酏酤酢酡酩酯酽酾酲酴酹醅醐醍醑醢醣醪醭醮醯醵醴醺豕鹾趸跫踅蹙蹩趵趿趼趺跄跖跗跚跞跎跏跛跆跬跷跸跣跹跻跤踉跽踔踝踟踬踮踣踯蹀踵踽踱蹉蹁蹂蹑蹒蹊蹰蹶蹼蹯蹴躅躏躔躐躜躞豸貂貊貅貘貔斛觖觞觚觜觥觫觯訾謦靓雩雳雯霆霁霈霏霎霪霭霰霾龀龃龅龆龇龈龉龊龌黾鼋鼍隹隼隽雎雒瞿雠銎銮鋈錾鍪鏊鎏鑫鱿鲂鲅鲈稣鲋鲎鲐鲒鲔鲕鲚鲛鲞鲟鲠鲡鲢鲣鲥鲦鲧鲨鲩鲫鲭鲮鲰鲱鲲鲳鲵鲶鲷鲻鲽鳄鳅鳆鳇鳌鳍鳎鳏鳐鳓鳔鳕鳗鳜鳝鳟鳢靼鞅鞑鞔鞯鞫鞣骱骰骷鹘骼髁髀髅髂髋髌髑魅魃魇魉魈魍魑飨餍餮饕饔髟髡髦髯髫髻髭髹鬈鬓鬟鬣麽麾縻麂麇麈麋麒鏖麝麟黛黜黝黠黟黩黧黥黪黯鼢鼬鼯鼹鼷鼽鼾";

	/**
	 * 难字替换列表
	 */
	static HashMap hardCode = new HashMap();

	/**
	 * 标识是否已实始化
	 */
	static boolean hadLoadHardCode = false;

	/**
	 * 中文英文长度统计函数,只检查长度,unicode不做内码转换
	 * 
	 * @param lineStr
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 */
	public static int checkLength(String lineStr) {
		int count = 0;
		if (empty(lineStr).length() > 0) {
			for (int i = 0; i < lineStr.length(); i++) {
				count += is2ByteCharset(lineStr.charAt(i)) ? 2 : 1;
			}
		}
		return count;
	}

	/**
	 * 检查传入字符是否中文或全型字符双字节,加入个别字符处理<br>
	 * 在unicode内部不用再区别分简繁字符
	 * 
	 * @param c
	 * @return 双字节则返回true,否则返回false
	 */
	public static boolean is2ByteCharset(char c) {

		return isDoubleChartSet(c) || isFullWidthChar(c);
	}

	/**
	 * 基本双字符检查,unicode内部不用再区别分简繁字符
	 * 
	 * @param c
	 * @return
	 */
	private static boolean isDoubleChartSet(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		if (!Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
				&& !Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)
				&& !Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block) && 12288 != c) {
			return false;
		}
		return true;
	}

	/**
	 * 全型字符检核函数，或个特别字符处理
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isFullWidthChar(char c) {
		return (!(('\uFF61' <= c) && (c <= '\uFF9F')) && !(('\u0020' <= c) && (c <= '\u007E')));
	}

	/**
	 * 中文难字mappinng,用于输出GBK,BIG5转档特殊需求,可定义在外部档
	 * 
	 */
	public static void initUTF8HardChar() {
		if (!hadLoadHardCode) {
			hardCode.put('廸', "  ");
			hadLoadHardCode = true;
		}
	}

	/**
	 * 输出简体编码,不进行简繁转换 <br>
	 * 2010/02/04 [14335] wing WREP报表修改调用
	 * 
	 * @param compl
	 * @return
	 */
	public static String complTosimpleNoMappingBig5(String compl) {
		String str = "";
		for (int i = 0; i < compl.length(); i++) {
			if (hardCode.containsKey(compl.charAt(i))) {
				str += (String) hardCode.get(compl.charAt(i));
				// 非简体字区间，直接输出空
			} else if (!isSimpleChineseCharacter(compl.charAt(i))) {
				str += "  ";
			} else {
				str += compl.charAt(i);
			}
		}
		return str;
	}

	/**
	 * 转为简体，具自动转换(繁体或难字）于简体字符功能,用于特别转档需求
	 * 
	 * @param compl
	 * @return
	 */
	public static String complTosimple(String compl) {
		String str = "";
		for (int i = 0; i < compl.length(); i++) {
			if (hardCode.containsKey(compl.charAt(i))) {
				str += (String) hardCode.get(compl.charAt(i));
				// 非简体字区间，直接输出空
			} else if (!isSimpleChineseCharacter(compl.charAt(i))) {
				str +=  "  ";
				// 将部分的繁体通过对比
			} else if (emisUTF8StringUtil.turnCompl.indexOf(compl.charAt(i)) != -1)
				str += emisUTF8StringUtil.turnSimpe.charAt(emisUTF8StringUtil.turnCompl.indexOf(compl
						.charAt(i)));
			else
				str += compl.charAt(i);
		}
		return str;
	}

	/**
	 * 检查是否为基本字符与简体, 仅用于转档，或ms950,gbk盘点档使用，检查中文长度不使用本方方法 简体中文区间：u4e00到\u9fa5
	 * 一个字符是否是简体中文， 调用于只转为简体输出之complTosimple()调用
	 * 
	 * @param charData
	 * @return
	 */
	public static boolean isSimpleChineseCharacter(char charData) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(charData);
		if (Character.UnicodeBlock.of(charData) == Character.UnicodeBlock.BASIC_LATIN) {
			return true;
		}
		// \u4e00到\u9fa5 一个字符是否是简体中文，注意这个不是u9faff
		//基本的数据在这里已经返回，不会再new String String
		int chinaChar = (int) charData;
		if (chinaChar >= 19968 && chinaChar <= 171941) {
			return true;
		}
        
		if (!Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
				&& !Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)
				&& !Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)
				&& !Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(block)
				&& 12288 != charData) {
			return false;
		}
		try {
			String str = "" + charData;
			return str.equals(new String(str.getBytes("GBK"), "GBK"));
		} catch (java.io.UnsupportedEncodingException e) {
			return false;
		}

	}

	/**
	 * 仅用于转档，或ms950,gbk盘点档使用，检查中文长度不使用本方方法 转为繁体字符串输出,不进行简繁转换
	 * 
	 * @param compl
	 * @return
	 */
	public static String complToMS950NoMappingGBK(String compl) {
		String str = "";
		for (int i = 0; i < compl.length(); i++) {

			if (hardCode.containsKey(compl.charAt(i))) {
				str += (String) hardCode.get(compl.charAt(i));
			} else if (!isTraditionalChineseCharacter(compl.charAt(i))) {
				str += "  ";
			} else {
				str += compl.charAt(i);
			}
		}
		return str;
	}

	/**
	 * 仅用于转档，或ms950,gbk盘点档使用，检查中文长度不使用本方方法 turnCompl turnSimpe 转为繁体字符串输出
	 * 
	 * @param compl
	 * @return
	 */
	public static String complToMS950(String compl) {
		String str = "";
		for (int i = 0; i < compl.length(); i++) {

			if (hardCode.containsKey(compl.charAt(i))) {
				str += (String) hardCode.get(compl.charAt(i));
			} else if (!isTraditionalChineseCharacter(compl.charAt(i))) {
				str += "  ";
			} else if (turnSimpe.indexOf(compl.charAt(i)) != -1)
				str += turnCompl.charAt(turnSimpe.indexOf(compl.charAt(i)));
			else
				str += compl.charAt(i);
		}
		return str;
	}

	// 删除complFilterNoUTF8CharsetToMS950

	/**
	 * 设定若参数为 null或为空字符，回传空字符串""
	 */
	public static String empty(Object obj) {
		return empty(obj, "");
	}

	/**
	 * 设定若参数为 null或为空字符，回传第二个参数
	 */
	public static String empty(Object obj, String toString) {
		if (obj == null) {
			return toString;
		} else {
			return (String) obj;
		}
	}

	/**
	 * 若参数为 null 回传 空字符串"";
	 */
	public static String escapeNull(Object obj) {
		return escapeNull(obj, "");
	}

	/**
	 * 设定若参数为 null，回传第二个参数
	 */
	public static String escapeNull(Object obj, String toString) {
		if (obj == null) {
			return toString;
		} else {
			return (String) obj;
		}
	}

	//删除isOnlyTraditionalChineseCharacter 

	/**
	 * 基本字符与繁体中文检核函数 仅用于转档，或ms950,gbk盘点档使用，检查中文长度不使用本方方法
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isTraditionalChineseCharacter(char charData) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(charData);
		/**
		 * 注意基本字符函数的检查
		 */
		if (Character.UnicodeBlock.of(charData) == Character.UnicodeBlock.BASIC_LATIN) {
			return true;
		}
		if (!Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
				&& !Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)
				&& !Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)
				&& !Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(block)
				&& 12288 != charData) {
			return false;
		}
		try {
			String s = "" + charData;
			return s.equals(new String(s.getBytes("MS950"), "MS950"));
		} catch (java.io.UnsupportedEncodingException e) {
			return false;
		}

	}

	/**
	 * subString() 以 byte 数取子字符串 传入: String s => 来源字符串 int beginIndex => 开始位组位置
	 * int length => 欲取位组长度 传回: String
	 */
	public static String subStringB(String s, int beginIndex, int length) {
		if (s == null)
			s = "";
		String _sRet = ""; // 传回字符串
		int _iByteOff = 0; // 以 byte 计算的累进位置
		int _iNumBytes = 0; // 取得 byte 数

		try {
			StringBuffer sb = new StringBuffer(length);

			for (int i = 0; i < s.length(); i++) {

				// 判断字符是否为中文
				if (Character.UnicodeBlock.of(s.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN) {
					// 汉字字符
					if (_iByteOff == (beginIndex - 1)) {
						// 第一byte为中文字后半开始, 该字符不取, 以 1byte 空白取代
						sb.append(" ");
						_iNumBytes++;
					} else if (_iByteOff >= beginIndex) {
						if (_iNumBytes + 2 > length) {
							// 已达欲取长度, 最后为中文字的前半, 该字符不取, 以 1byte 空白取代
							sb.append(" ");
							_iNumBytes++;
						} else {
							sb.append(s.charAt(i));
							_iNumBytes += 2;
						}
					}
					_iByteOff += 2; // 汉字字符 byte 位置累进 2
				} else {
					// 非汉字字符
					if (_iByteOff >= beginIndex) {
						sb.append(s.charAt(i));
						_iNumBytes++;
					}
					_iByteOff++; // 非汉字字符 byte 位置累进 1
				}

				if (_iNumBytes >= length)
					break; // 已达欲取长度, 结束回圈
			} // end for
			_sRet = sb.toString();
		} catch (Exception e) {
		}

		return _sRet;

	} // end subStringB()

	/**
	 * 补空，调用，同上, 在emis部分作业中不指定长度，超长部分也不需要截短（即保留)
	 * 
	 * @param str_
	 * @param align_
	 * @param size_
	 * @return
	 */
	public static String padding(String str_, int align_, int size_) {
		int nLen = emisUTF8StringUtil.checkLength(str_);
		if (nLen >= size_) {
			// already exceed , truncate it ?
			return str_; // 超長則不處理
		}
		return align(str_, size_, align_);
	}

	/**
	 * 对齐方式设定,注意这个会截长，与padding方法是不同的, 因为padding方法在emis部分作业中不指定长度，超长部分也不需要截短（即保留)
	 * 
	 * @param str
	 * @param length
	 * @param align
	 * @return
	 */
	public static String align(String str, int length, int align) {

		return align(str, length, align, SPACE_SIGN);
	}

	/**
	 * 对齐方式,补齐不同符号指定,超长部分截短
	 * 
	 * @param str
	 * @param length
	 * @param align
	 * @param padding
	 * @return
	 */
	public static String align(String str, int length, int align, char padding) {
		if (align == 1) {
			String res = alignLeft(str, length, padding);
			return res;
			// return alignLeft(str, length, padding);
		} else if (align == 2) {
			return alignCenter(str, length, padding);
		} else if (align == 3) {
			return alignRight(str, length, padding);
		}

		return str;
	}

	/**
	 * 居中并补齐,超长部分截短
	 * 
	 * @param str
	 * @param length
	 * @param cPad
	 * @return
	 */
	public static String alignCenter(String str, int length) {
		return allPadB(str, length, SPACE_SIGN);
	}

	/**
	 * 居中并补齐,补齐不同符号指定,超长部分截短
	 * 
	 * @param str
	 * @param length
	 * @param cPad
	 * @return
	 */
	public static String alignCenter(String s, int length, char charAt) {
		return allPadB(s, length, charAt);
	}

	/**
	 * 左对齐,超长部分截短
	 * 
	 * @param s
	 * @param length
	 * @param cPad
	 * @return
	 */
	public static String alignLeft(String s, int length) {
		return rightPadB(s, length, ' ');
	}

	/***************************************************************************
	 * 补左,超长部分截短
	 * 
	 * @param s
	 * @param length
	 * @param charAt
	 * @return
	 */
	public static String alignLeft(String s, int length, char charAt) {

		return rightPadB(s, length, charAt);
	}

	/**
	 * 右对齐,超长部分截短
	 * 
	 * @param s
	 * @param length
	 * @param cPad
	 * @return
	 */
	public static String alignRight(String s, int length, char charAt) {
		return leftPadB(s, length, charAt);
	}

	/**
	 * 
	 * @param s
	 * @param length
	 * @return
	 */
	public static String alignRight(String s, int length) {
		return leftPadB(s, length, ' ');
	}

	/**
	 * 保留超长部分处理
	 * 
	 * @param s
	 * @param length
	 * @param cPad
	 * @return
	 */
	public static String allPadB(String s, int length, char cPad) {
		if (s == null)
			s = "";
		String _sRet = "";
		int _n = length - checkLength(s);

		if (_n <= 0) {
			_sRet = subStringB(s, 0, length);
		} else {
			_sRet = replicate(cPad, _n / 2) + s + replicate(cPad, (_n - _n / 2));
		}
		return _sRet;
	}

	/**
	 * 以传入长度来填左方字符.
	 * 
	 * @param s
	 * @param length
	 * @param cPad;
	 *            要填入的字符, 预设是空白
	 * @return 填完后之字符串
	 */
	public static String leftPadB(String s, int length, char cPad) {
		if (s == null)
			s = "";
		String _sRet = "";
		int _n = length - checkLength(s);

		if (_n <= 0) {
			_sRet = subStringB(s, 0, length);
		} else {
			_sRet = replicate(cPad, _n) + s;
		}
		return _sRet;
	}

	/**
	 * 以传入长度来填右方字符.
	 * 
	 * @param s
	 * @param length
	 * @param cPad;
	 *            要填入的字符, 预设是空白
	 * @return 填完后之字符串
	 */
	public static String rightPadB(String s, int length, char cPad) {
		if (s == null)
			s = "";
		String _sRet = "";
		int _n = length - checkLength(s);

		if (_n <= 0) {
			_sRet = subStringB(s, 0, length);
		} else {
			_sRet = s + replicate(cPad, _n);
		}
		return _sRet;
	}

	/**
	 * 产生指定长度(length)由传入字符(length)组成的字符串
	 * 
	 * @param c
	 * @param length
	 * @return
	 */
	public static String replicate(char c, int length) {
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; i++)
			sb.append(c);

		return sb.toString();
	}

	/**
	 * 格式化公元/民国日期字符串
	 * 
	 * @param sDate:
	 *            日期字符串
	 * @param sFormat:
	 *            格式化的日期字符串
	 * @return 已格式化的日期字符串 ex: formatDate("20030101","yyyy/MM/dd") return
	 *         "2003/01/01" formatDate("0930101","yyy-MM-dd") return "093-01-01"
	 *         formatDate("0930101","yyy年MM月dd日") return "093年01月01日"
	 * 
	 * 传入空的格式化的日期字符串可用来移除现在格式 ex: formatDate("2003/01/01","") return "20030101"
	 * formatDate("093-01-01","") return "0930101"
	 */
	public static String formatDate(String sDate, String sFormat) {
		// 进行有效检查
		if ((sDate == null) || (sDate.length() == 0))
			return "";

		String str = "";
		// 从传入之日期字符串是过滤出数值类型值
		for (int i = 0; i < sDate.length(); i++) {
			if (Character.isDigit(sDate.charAt(i)))
				str += sDate.charAt(i);
		}
		// 当提取出来的日期字符串超出8码，则为无效日期，直接返回空值
		if (str.length() > 8)
			return "";
		// 当不足7码时前置补0到7码
		if (str.length() < 7)
			str = leftPadB(str, 7, '0');

		if (sFormat != null && !sFormat.equals("")) {
			// 取出年、月、日
			int iIndex = (str.length() == 7 ? 3 : 4);
			String sYear = str.substring(0, iIndex);
			String sMonth = str.substring(iIndex, iIndex += 2);
			String sDay = str.substring(iIndex, iIndex += 2);
			// 将取出的年、月、日回填，产生指定格式日期值
			sFormat = sFormat.replaceAll("yyyy", sYear);
			sFormat = sFormat.replaceAll("yyy", sYear);
			sFormat = sFormat.replaceAll("MM", sMonth);
			sFormat = sFormat.replaceAll("M", sMonth);
			sFormat = sFormat.replaceAll("dd", sDay);
			sFormat = sFormat.replaceAll("d", sDay);
			// 传回格式化后的日期
			str = sFormat;
		}
		return str;
	}

	/**
	 * 将 Date 对象与以格式化后传回日期时间字符串，格式化字符串可用的样式请参考 SimpleDateFormat， 此外，本方法增加民国年的样式:
	 * eee。 注意：民国前的年份会传回负数，例如：-001/3/26。 使用范例： s = DateUtil.formatDateTime(new
	 * Date(), "eee/mm/dd");
	 * 
	 * @param aDate
	 *            日期物件
	 * @param aFormat
	 *            格式化字符串
	 * @return 格式化后的日期时间字符串
	 */
	public static String formatDate(Date aDate, String aFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat();
		int year;
		int yearPos;
		int yearCnt = 0;
		int i;
		char token;
		boolean useROCYear = false;

		cal.setTime(aDate);
		year = cal.get(Calendar.YEAR);

		// 找出年份的格式化 token 字符串的起始位置及字符数
		i = 0;
		yearPos = -1;
		while (i < aFormat.length()) {
			token = aFormat.charAt(i);
			if ((token == 'e') || (token == 'y')) {
				if (token == 'e') {
					useROCYear = true;
				}
				yearPos = i;
				do {
					i++;
				} while ((i < aFormat.length()) && (token == aFormat.charAt(i)));
				yearCnt = i - yearPos;
				break;
			}
			i++;
		}

		// 若格式化字符串中没有指定年，就直接用 Java 标准 API 进行转换
		if (yearCnt == 0) {
			df.applyPattern(aFormat);
			return df.format(aDate);
		}

		// 将年份与其它部分拆开各自转换，然后再组合起来
		String beforeYear = aFormat.substring(0, yearPos);
		String afterYear = aFormat.substring(i, aFormat.length());
		df.applyPattern(beforeYear);
		String s1 = df.format(aDate);
		df.applyPattern(afterYear);
		String s2 = df.format(aDate);

		String yearStr = "";
		if (useROCYear) {
			if (yearCnt < 2) { // 年份位数太少时，自动予以修正
				yearCnt = 2;
			}
			year = year - 1911;
			if (year <= 0) // 没有民国 0 年
				year--;
			yearStr = formatNumber(year, yearCnt);

		} else { // 公元年
			if (yearCnt == 3) { // 修正年份位数
				yearCnt = 4;
			}
			yearStr = formatNumber(year, yearCnt);
		}
		return s1 + yearStr + s2;
	}

	/**
	 * 将传入的数字格式化 aDigits 位数的字符串，不满位数则补零
	 * 
	 * @param aNumber
	 * @param aDigits
	 * @return
	 */
	private static String formatNumber(int aNumber, int aDigits) {
		StringBuffer sbFmt = new StringBuffer();
		NumberFormat formatter;

		for (int i = 0; i < aDigits; i++) {
			sbFmt.append("0");
		}

		formatter = new DecimalFormat(sbFmt.toString());
		return formatter.format(aNumber);
	}

	/**
	 * 取回 int 形態值
	 * 
	 * @param num
	 * @return
	 */
	public static int intValue(Object num) {
		return checkInteger(escapeNull(num)).intValue();
	}

	/**
	 * 取回 double 形態值
	 * 
	 * @param num
	 * @return
	 */
	public static double doubleValue(Object num) {
		return checkDouble(escapeNull(num)).doubleValue();
	}

	/**
	 * 取回 long 形態值
	 * 
	 * @param num
	 * @return
	 */
	public static long longValue(Object num) {
		return checkLong(escapeNull(num)).longValue();
	}

	/**
	 * 取回 int 形態值
	 * 
	 * @param num
	 * @return
	 */
	public static int double2int(Object num) {
		return checkDouble(escapeNull(num)).intValue();
	}

	/**
	 * 取回 long 形態值
	 * 
	 * @param num
	 * @return
	 */
	public static long double2long(Object num) {
		return checkDouble(escapeNull(num)).longValue();
	}

	/**
	 * 檢查Integer形態，並返回Integer
	 * 
	 * @param str
	 * @return
	 */
	public static Integer checkInteger(String str) {
		Integer i = new Integer(0);
		if (str != null || !"".equals(str)) {
			try {
				i = Integer.valueOf(str);
			} catch (NumberFormatException e) {
			}
		}
		return i;
	}

	/**
	 * 檢查Long形態，並返回Long
	 * 
	 * @param str
	 * @return
	 */
	public static Long checkLong(String str) {
		Long i = new Long(0);
		if (str != null || !"".equals(str)) {
			try {
				i = Long.valueOf(str);
			} catch (NumberFormatException e) {
			}
		}
		return i;
	}

	/**
	 * 檢查Double形態，並返回Double
	 * 
	 * @param str
	 * @return
	 */
	public static Double checkDouble(String str) {
		Double i = new Double(0);
		if (str != null || !"".equals(str)) {
			try {
				if (str.indexOf(",") > 0)
					str = str.replaceAll(",", "");
				i = Double.valueOf(str);
			} catch (NumberFormatException e) {
			}
		}
		return i;
	}

	/**
	 * 數值金額格形化,默認為0.##
	 * 
	 * @param d
	 * @param pattern
	 * @return
	 */
	public static String doubleFormat(double d) {
		return doubleFormat(d, "0.00");
	}

	public static String doubleFormat(double d, String pattern) {
		if (pattern == null || "".equals(pattern))
			pattern = "0.##";
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(d);
	}

	/**
	 * 创建输出文件类型,UTF-8,GBK,MS950
	 * 
	 * @param fileOutEncodeType
	 * @param fileName
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public static PrintWriter createWriter(String fileOutEncodeType, String fileName)
			throws UnsupportedEncodingException, FileNotFoundException {

		// 删除注解
		return new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),
				fileOutEncodeType)));

	}

	/**
	 * [14291] 20100205,ServletContext 据参数获得,epos_src作业使用了本方法
	 * epos emisHTParseCnt.java
	 */
	public static BufferedReader getReaderForSaleUpload(emisFile oTextFileObj_, ServletContext oContext_)
			throws Exception {
		String ccrOutFileCharSet = "MS950";
		if (oContext_ != null) {
			ccrOutFileCharSet = emisProp.getInstance(oContext_).get("CCR_OUTFILE_CHARSET", "MS950");
		}
		FileInputStream fin = (new FileInputStream(oTextFileObj_.getFullName()));
		InputStreamReader _oFReader = new InputStreamReader(fin, ccrOutFileCharSet);

		try {
			return new BufferedReader(_oFReader);
		} catch (Exception e2) {
			try {
				_oFReader.close();
			} catch (Exception ignore3) {
			}
			throw e2;
		}
	}

	public static void main(String args[]) {
		String content = "                  ";
		content = "1廸1廸廸1";
		System.out.println("xcontent$" + emisUTF8StringUtil.checkLength(content) + "$length");
		String content2 = emisString.subStringB(content, 0, 2);
		System.out.println("emisString lengthB 1$:" + emisString.lengthB(content2));
		System.out.println("emisString content2$" + content2 + "$");

		System.out.println("content2 checkLength$:" + emisUTF8StringUtil.checkLength(content2) + ":length");
		String subLen = emisUTF8StringUtil.subStringB(content, 0, 1);
		System.out.println("emis clen$" + emisChinese.clen(subLen));
		System.out.println("emisUTF8StringUtil subStringB$" + subLen + "$");
		System.out.println("emisUTF8StringUtil checkLength$" + emisUTF8StringUtil.checkLength(subLen) + ":length");
	}
}

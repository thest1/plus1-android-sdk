Plus1 WapStart Android SDK
==========================
Это open source библиотека для интеграции рекламы сети [Plus1 WapStart](https://plus1.wapstart.ru) в ваши Android-приложения.

Plus1 WapStart Android SDK распространяется под свободной лицензией BSD (as is).

**Оглавление:**
* [Установка и настройка](#Установка-и-настройка)
  * [Конфигурирование манифеста](#Конфигурирование-манифеста)
  * [Настройка тестового приложения](#Настройка-тестового-приложения)
* [Использование SDK](#Использование-SDK)
  * [Добавление баннера в приложение](#Добавление-баннера-в-приложение)
  * [Обработка событий от SDK в приложении](#Обработка-событий-от-SDK-в-приложении)
  * [Разрешение коллизий работы WebView в приложении](#Разрешение-коллизий-работы-webview-в-приложении)
* [Контактная информация](#Контактная-информация)


# Установка и настройка

1. Скачайте последнюю версию SDK: https://github.com/WapStart/plus1-android-sdk/tags
2. Для начала работы необходимо добавить SDK к проекту в качестве библиотеки;
3. Сконфигурируйте манифест согласно нижеприведённым инструкциям.

## Конфигурирование манифеста
В первую очередь необходимо добавить собственную url-схему и хост. Это необходимо для возврата из браузера обратно в приложение после синхронизации cookie пользователя.

Пример:

```xml
<intent-filter>
	<data android:scheme="wsp1bart" android:host="ru.wapstart.plus1.bart" />
	<action android:name="android.intent.action.VIEW"/>
	<category android:name="android.intent.category.DEFAULT" />
	<category android:name="android.intent.category.BROWSABLE" />
</intent-filter>
```

Для тестового приложения в качестве схемы выступает *wsp1bart://*

**Внимание:** сочетание схема-хост должно быть уникально для гарантии возврата в ваше приложение.

Для корректной работы SDK приложение должно обладать правами на доступ к сети Интернет и на получение текущего местоположения:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

При этом **ACCESS_FINE_LOCATION** не является обязательным, но рекомендуется для подбора релевантных рекламных объявлений.

Если ваше приложение использует геолокацию, вы можете самостоятельно устанавливать текущее местоположение (см. описание интерфейсов - [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md)). При этом рекомендуется отключать автоматическое определение местоположения в SDK (метод *disableAutoDetectLocation()* в [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md)).

В блок *<application>* требуется добавить информацию об используемых Activity:

```xml
<application android:label="Bart" android:icon="@drawable/icon">
	<activity android:name="<app Activity name>" ... >
		...
	</activity>

	<activity android:name="ru.wapstart.plus1.sdk.ApplicationBrowser" />
</application>
```

*[ApplicationBrowser](https://github.com/WapStart/plus1-android-sdk/blob/master/sdk/src/ru/wapstart/plus1/sdk/ApplicationBrowser.java)* используется для перехода по ссылкам баннеров внутри приложения.

## Настройка тестового приложения
Для работы тестового приложения вам нужно передать идентификатор площадки [Plus1 WapStart](https://plus1.wapstart.ru) в методе **setApplicationId()** в файле *[BartActivity.java](https://github.com/WapStart/plus1-android-sdk/blob/master/examples/Bart/src/ru/wapstart/plus1/bart/BartActivity.java#L51)*.

Идентификатор площадки можно узнать на странице **Код для площадки** после регистрации в сети [Plus1 WapStart](https://plus1.wapstart.ru) и добавления площадки типа Android.


# Использование SDK

Примеры настройки и конфигурации баннеров можно посмотреть в тестовом приложении **Bart**. В этом разделе даются краткие пояснения для быстрой настройки собственного проекта.

## Добавление баннера в приложение
Добавьте [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) в layout:

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    >
	<ru.wapstart.plus1.sdk.Plus1BannerView
			android:id="@+id/plus1BannerView"
			android:layout_width="320dp"
			android:layout_height="50dp"
			android:layout_gravity="top|center"
		/>
</FrameLayout>

```
View адаптировано под размер 320x50. Рекомендуется использовать именно данные параметры.

Подключите необходимые классы в файле вашего Activity, где планируете показывать рекламу:

```java
import ru.wapstart.plus1.sdk.Plus1BannerView;
import ru.wapstart.plus1.sdk.Plus1Request;
import ru.wapstart.plus1.sdk.Plus1BannerAsker;
```

На этапе инициализации создайте и настройте объекты [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md) и [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) следующим образом:

```java
@Override
protected void onCreate(Bundle savedInstanceState)
{
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	...

	mBannerView =
		(Plus1BannerView) findViewById(R.id.plus1BannerView);

	mAsker =
		new Plus1BannerAsker(
			new Plus1Request()
				.setApplicationId(...),
			mBannerView
				.enableAnimationFromTop()
				.enableCloseButton()
		)
		.setCallbackUrl(...)

}
```

В методе **setApplicationId()** задайте идентификатор вашей рекламной площадки. Его можно узнать на странице **Код для площадки** после регистрации в сети [Plus1 WapStart](https://plus1.wapstart.ru) и добавления площадки типа Android.

Установите *callback url* методом **setCallbackUrl()** согласно вашим настройкам манифеста.

Пример:

```java
        mAsker =
                new Plus1BannerAsker(
			...
                )
                .setCallbackUrl("wsp1bart://ru.wapstart.plus1.bart")

}
```

Затем необходимо предусмотреть вызов обработчиков *onResume()* и *onPause()* класса [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md):

```java
@Override
protected void onResume() {
	super.onResume();

	...

	mAsker.onResume();
}

@Override
protected void onPause() {
	super.onPause();

	...

	mAsker.onPause();
}
```

Для баннеров формата Rich Media в [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) необходимо передавать событие нажатия клавиши "Назад":

```java
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if ((keyCode == KeyEvent.KEYCODE_BACK) && mBannerView.canGoBack()) {
		mBannerView.goBack();
		return true;
	}

	return super.onKeyDown(keyCode, event);
}
```

Для подробного ознакомления смотрите описания интерфейсов и классов, а также исходные коды sdk:
* [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md) - отвечает за получение объявлений с сервера
* [Plus1Request](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1Request.md) - отвечает за хранение информации о пользователе и формирование запроса к серверу
* [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) - отвечает за отображение объявления
* [Plus1BannerDownloadListener](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerDownloadListener.md) - интерфейс наблюдателя загрузки баннера

## Обработка событий от SDK в приложении
Часто возникают ситуации, когда в приложении необходима реакция на какое-то событие, связанное с баннером. Для этого в [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md) предусмотрены *наблюдатели*:

```java
public Plus1BannerView addListener(OnShowListener listener);
public Plus1BannerView addListener(OnHideListener listener);
public Plus1BannerView addListener(OnCloseButtonListener listener);
public Plus1BannerView addListener(OnExpandListener listener);
public Plus1BannerView addListener(OnCollapseListener listener);
public Plus1BannerView addListener(OnImpressionListener listener);
public Plus1BannerView addListener(OnTrackClickListener listener);
```

Для обработки событий в приложении необходимо добавить нужный *наблюдатель* для [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md). Например:

```java
mBannerView
	.addListener(new Plus1BannerView.OnShowListener() {
		public void onShow(Plus1BannerView pbv) {
			Log.d(LOGTAG, "Рекламный блок появился на экране");
		}
	})
	.addListener(new Plus1BannerView.OnHideListener() {
		public void onHide(Plus1BannerView pbv) {
			Log.d(LOGTAG, "SDK скрыл рекламный блок");
		}
	})
	.addListener(new Plus1BannerView.OnTrackClickListener() {
		public void onTrackClick(Plus1BannerView pbv) {
			Log.d(LOGTAG, "Произошло событие нажатия на баннер");
		}
	});
```

Описание всех *наблюдателей* смотрите в [Plus1BannerView](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerView.md#Наблюдатели).

## Разрешение коллизий работы WebView в приложении
Если вы используете *WebView* в разных Activity вашего приложения, то логика работы этого компонента будет нарушаться. Причины коллизии в том, что по умолчанию sdk вызывает обработчики [pauseTimers()](http://developer.android.com/reference/android/webkit/WebView.html#pauseTimers%28%29) и [resumeTimers()](http://developer.android.com/reference/android/webkit/WebView.html#resumeTimers%28%29) класса *WebView* в соответствующих контекстах событий *onPause* и *onResume* в Activity вашего приложения. Вызовы этих методов влияют на все экземпляры *WebView* и позволяют исключить обработку *WebView* в то время, когда ваше приложение (Activity) не показывается пользователю.

Для разрешения этой коллизии предлагается воспользоваться методом **setDisabledWebViewCorePausing()** класса [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md). При этом важно понимать, что *WebView* продолжает работать в то время, когда Activity с баннером приостановлена. Для экономии ресурсов процессора рекомендуем также ограничить использование *WebView* в Activity используя метод **setRemoveBannersOnPause()** класса [Plus1BannerAsker](https://github.com/WapStart/plus1-android-sdk/blob/master/doc/Plus1BannerAsker.md).

# Контактная информация

По всем возникающим у вас вопросам интеграции вы можете обратиться в службу поддержки пользователей:  
E-Mail: clientsupport@co.wapstart.ru  
ICQ: 553425962

---------------------------------------
Мы постоянно улучшаем наши SDK, делаем их удобнее и стабильнее. Будем рады вашему [участию в разработке](https://github.com/Wapstart/plus1-android-sdk/pulls), с радостью рассмотрим и обсудим [ваши предложениия](https://github.com/WapStart/plus1-android-sdk/issues)!

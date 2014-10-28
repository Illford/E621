package illford.e621;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     RecyclerView mRecyclerView;
     MyAdapter mAdapter;
     RecyclerView.LayoutManager mLayoutManager;
    int width;
    int height;
    int page=1;
    String search="";
    boolean useFull;
    int colcount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ConnectivityManager cm =(ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        String url="https://e621.net/post/index.xml";
        Intent intent = getIntent();
        if(intent.getStringExtra(MyActivity.PAGE)!=null)
        page=Integer.parseInt(intent.getStringExtra(MyActivity.PAGE));
        url+="?page="+page;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher2);
       toolbar.setTitle("");
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
                in.close();
                html = str.toString();
                Pattern p;
                 p= Pattern.compile("file_url=\"(.*?)\"");
                Matcher m=p.matcher(html);
                while( m.find()){
                    if(!m.group(1).contains(".swf")){
                        fullImageUrl.add(m.group(1));
                      }
                }
                p= Pattern.compile("preview_url=\"(.*?)\"");
                 m=p.matcher(html);
                while( m.find()){
                    if(!m.group(1).contains(".swf")){
                        thumbImageUrl.add(m.group(1));
                    }
                }


            }
            catch(IOException e){
                  final String TAG = "MyActivity";
                Log.e(TAG,"ERROR:  "+e);
            }
            return html;
        }

        protected void onPostExecute(String result) {
                add();



        }
    }
    public void add(){
        mAdapter.notifyItemInserted(fullImageUrl.size());

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
            if(useFull)
            Picasso.with(context).load(fullImageUrl.get(position)).placeholder(R.drawable.ic_action_refresh).resize(width/colcount,height/colcount).centerInside().into(holder.mImageView);
            else
                Picasso.with(context).load(thumbImageUrl.get(position)).placeholder(R.drawable.ic_action_refresh).resize(width/colcount,height/colcount).centerInside().into(holder.mImageView);
            holder.mPos=position;

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    zoomImageFromThumb(holder.mImageView,fullImageUrl.get(position) );
                }
            });
            mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        }
        private void zoomImageFromThumb(final View thumbView,String URL){
            final ImageView zoomView=(TouchImageView)findViewById(R.id.expanded_image);
            if(mCurrentAnimator!=null){
                mCurrentAnimator.cancel();
            }
            Picasso.with(context).load(URL).placeholder(R.drawable.ic_action_refresh).into(zoomView);
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
        }
    }


}

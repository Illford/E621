package illford.e621;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

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


public class MyActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.illford.e621.MESSAGE";
    String html = "";
    ArrayList<String> fullImageUrl= new ArrayList<String>();
     RecyclerView mRecyclerView;
     MyAdapter mAdapter;
     RecyclerView.LayoutManager mLayoutManager;
    int width;
    int height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String url="https://e621.net/post/index.xml";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        SharedPreferences sharedPref =this.getSharedPreferences("com.illford.e621",Context.MODE_PRIVATE);
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
        Intent intent = getIntent();
        String message = intent.getStringExtra(MyActivity.EXTRA_MESSAGE);
        url+=message;
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
        mLayoutManager = new LinearLayoutManager(this);
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
        else if(id ==R.id.search){
            searchdialog();

        }
        return super.onOptionsItemSelected(item);
    }
    public void startSettings(){
        Intent intent = new Intent(this, settings.class);
        startActivity(intent);
    }
    public void searchdialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final LayoutInflater inflater = this.getLayoutInflater();
        final View v=inflater.inflate(R.layout.searchdialog, null);
        builder.setView(v)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String searchtxt="";
                        EditText et=(EditText)v.findViewById(R.id.editsearch);
                        String BLtxt=et.getText().toString();

                        Pattern p= Pattern.compile("(\\w+)");
                        Matcher m=p.matcher(BLtxt);
                        while( m.find()){
                            searchtxt+="+"+m.group(1);

                        }
                        Intent intent = new Intent(MyActivity.this,MyActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, searchtxt);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_my, container, false);

        }
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
                Pattern p= Pattern.compile("file_url=\"(.*?)\"");
                Matcher m=p.matcher(html);
                while( m.find()){
                    if(!m.group(1).contains(".swf")){
                        fullImageUrl.add(m.group(1));
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
            public ImageView mImageView;
            public int mPos;
            public ViewHolder(View v) {
                super(v);
                mImageView = (ImageView) v.findViewById(R.id.image);

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
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Picasso.with(context).setIndicatorsEnabled(true);
            Picasso.with(context).load(fullImageUrl.get(position)).placeholder(R.drawable.ic_action_refresh).resize(width,height).centerInside().into(holder.mImageView);
            holder.mPos=position;
        }
    }




}

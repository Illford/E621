package illford.e621;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class settings extends Activity {
CheckBox CB;//NSFW content
    CheckBox CB2;//reduced bandwidth
    Boolean NSFW;
    Boolean forcefull;
    SharedPreferences sharedPref;
      SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         sharedPref =this.getSharedPreferences("com.illford.e621",Context.MODE_PRIVATE);
       editor = sharedPref.edit();
        setContentView(R.layout.activity_settings);
        CB = (CheckBox)findViewById(R.id.NSFWcb);
        CB2=(CheckBox)findViewById(R.id.datacb);
        EditText et=(EditText)findViewById(R.id.editblacklist);
        String BL="";
        Set<String>blset=sharedPref.getStringSet("blacklist",new HashSet<String>());
        String[] BLset=blset.toArray(new String[blset.size()]);
        for(int x=0;x<BLset.length;x++){
            BL+=","+BLset[x];
        }
        et.setText(BL);
         NSFW = sharedPref.getBoolean("NSFW",false);
        forcefull=sharedPref.getBoolean("forcefull",false);
        if(NSFW){
            CB.setChecked(true);
        }
        if(!forcefull){
            CB2.setChecked(true);
        }
        CB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                          @Override
                                          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                              if (isChecked) {

                                                  new AlertDialog.Builder(settings.this)
                                                      .setMessage(R.string.NSFWmssg)
                                                          .setCancelable(false)
                                                      .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int which) {
                                                              editor.putBoolean("NSFW",true);
                                                              editor.apply();
                                                          }
                                                      })
                                                      .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                          public void onClick(DialogInterface dialog, int which) {
                                                              CB.setChecked(false);
                                                          }
                                                      })
                                                      .setIcon(android.R.drawable.ic_dialog_alert)
                                                      .show();
                                              }
                                              else{ editor.putBoolean("NSFW",false);
                                                  editor.apply();

                                              }
                                          }
                                      }
        );
        CB2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                          @Override
                                          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                              if (isChecked) {
                                                  editor.putBoolean("forcefull",false);
                                                  editor.apply();
                                              }
                                              else{
                                                  editor.putBoolean("forcefull",true);
                                                  editor.apply();
                                              }
                                          }
                                      }
        );


     }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
@Override
    public void onStop(){
        super.onStop();
    EditText et=(EditText)findViewById(R.id.editblacklist);
    ArrayList <String> BLlist=new ArrayList<String>();
        String BLtxt=et.getText().toString();

    Pattern p= Pattern.compile("(\\w+)");
    Matcher m=p.matcher(BLtxt);
    while( m.find()){
            BLlist.add(m.group(1));

    }
        editor.putStringSet("blacklist",new HashSet<String>(BLlist));
    editor.apply();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

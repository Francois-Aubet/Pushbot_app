package de.tum.nst.pushbotcontrol;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



/*
    This is here to enter the ip address and the name of a newly created bot.




 */
public class IPActivity extends Activity implements View.OnClickListener {
    private Button btnDone;
    EditText ipadd = null;
    EditText nameadd = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        final String ipaddr = null;

        ipadd = (EditText)findViewById(R.id.editIpNumber);

        nameadd = (EditText)findViewById(R.id.editNameBot);

        //put a name in the field

        String[] americanNames = {"Challenger", "Jessy", "David", "James", "Sarah", "Christopher", "Margaret",
                "Ronald", "Michelle", "Steven", "Deborah",
                "Joseph", "Kimberly", "Anthony", "Susan", "Jeff"}; //, "", "", "", "", "", "", "", "", "", "", ""

        int index = MainActivity.list.size();

        if(index < americanNames.length){
            nameadd.setText(americanNames[index-1]);
        } else{
            nameadd.setText("no name");
        }

        btnDone = (Button) findViewById(R.id.doneButton);
        btnDone.setOnClickListener(this);
    }



    @Override
    protected void onResume() {
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	super.onPause();
    }


    @Override
    public void onClick(View v) {
        String ip = ipadd.getText().toString();
        MainActivity.list.get(MainActivity.chosenone).setHostIP(ip);
        String name = nameadd.getText().toString();
        MainActivity.list.get(MainActivity.chosenone).nameofthebot = name;
        super.onBackPressed();
    }


}

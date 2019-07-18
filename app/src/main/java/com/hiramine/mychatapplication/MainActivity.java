package com.hiramine.mychatapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// チャットクライアントアクティビティへ遷移するためのボタン
		Button buttonGoToChat = (Button)findViewById( R.id.button_gotochat );
		buttonGoToChat.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				String strNickname = ( (EditText)findViewById( R.id.edittext_nickname ) ).getText().toString();
				if( strNickname.isEmpty() )
				{
					Toast.makeText( MainActivity.this, "Enter your nickname", Toast.LENGTH_SHORT ).show();
				}
				else
				{
					Intent intent = new Intent( MainActivity.this, ChatClientActivity.class );
					intent.putExtra( ChatClientActivity.EXTRA_NICKNAME, strNickname );

					startActivity( intent );
				}
			}
		} );
	}
}

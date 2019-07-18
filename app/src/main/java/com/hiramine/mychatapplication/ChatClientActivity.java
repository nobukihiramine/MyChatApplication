package com.hiramine.mychatapplication;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatClientActivity extends AppCompatActivity
{
	static class Message
	{
		private String m_strNickname;
		private String m_strMessage;
		private String m_strDate;

		public Message( String strNickname, String strMessage, String strDate )
		{
			m_strNickname = strNickname;
			m_strMessage = strMessage;
			m_strDate = strDate;
		}

		public String getNickname()
		{
			return m_strNickname;
		}

		public String getMessage()
		{
			return m_strMessage;
		}

		public String getDate()
		{
			return m_strDate;
		}
	}

	static class MessageListAdapter extends BaseAdapter
	{
		private ArrayList<Message> m_listMessage;
		private LayoutInflater     m_inflater;

		public MessageListAdapter( Activity activity )
		{
			super();
			m_listMessage = new ArrayList<Message>();
			m_inflater = activity.getLayoutInflater();
		}

		// リストへの追加
		public void addMessage( Message message )
		{
			m_listMessage.add( 0, message );    // 先頭に追加
			notifyDataSetChanged();    // ListViewの更新
		}

		// リストのクリア
		public void clear()
		{
			m_listMessage.clear();
			notifyDataSetChanged();    // ListViewの更新
		}

		@Override
		public int getCount()
		{
			return m_listMessage.size();
		}

		@Override
		public Object getItem( int position )
		{
			return m_listMessage.get( position );
		}

		@Override
		public long getItemId( int position )
		{
			return position;
		}

		static class ViewHolder
		{
			TextView textviewDate;
			TextView textviewNickname;
			TextView textviewMessage;
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent )
		{
			ViewHolder viewHolder;
			// General ListView optimization code.
			if( null == convertView )
			{
				convertView = m_inflater.inflate( R.layout.listitem_message, parent, false );
				viewHolder = new ViewHolder();
				viewHolder.textviewDate = (TextView)convertView.findViewById( R.id.textview_date );
				viewHolder.textviewNickname = (TextView)convertView.findViewById( R.id.textview_nickname );
				viewHolder.textviewMessage = (TextView)convertView.findViewById( R.id.textview_message );
				convertView.setTag( viewHolder );
			}
			else
			{
				viewHolder = (ViewHolder)convertView.getTag();
			}

			Message message = m_listMessage.get( position );
			viewHolder.textviewDate.setText( message.getDate() );
			viewHolder.textviewNickname.setText( message.getNickname() );
			viewHolder.textviewMessage.setText( message.getMessage() );

			return convertView;
		}
	}

	// 定数
	public static final  String EXTRA_NICKNAME = "NICKNAME";
	private static final String URI_SERVER     = "http://mychat1234.herokuapp.com/";

	// メンバー変数
	private String m_strNickname = "";
	private Socket m_socket;
	private EditText m_edittextMessage;
	MessageListAdapter m_messagelistadapter;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_chat_client );

		// 呼び出し元からパラメータ取得
		Bundle extras = getIntent().getExtras();
		if( null != extras )
		{
			m_strNickname = extras.getString( EXTRA_NICKNAME );
		}

		// ニックネームの表示
		TextView textviewNickname = (TextView)findViewById( R.id.textview_nickname );
		textviewNickname.setText( "Nickname : " + m_strNickname );

		// サーバーとの接続
		try
		{
			m_socket = IO.socket( URI_SERVER );
		}
		catch( URISyntaxException e )
		{    // IO.socket失敗
			e.printStackTrace();
			Toast.makeText( this, "URI is invalid.", Toast.LENGTH_SHORT ).show();
			finish();    // アクティビティ終了
			return;
		}
		m_socket.connect();    // 接続

		// 接続完了時の処理
		m_socket.on( Socket.EVENT_CONNECT, new Emitter.Listener()
		{
			@Override
			public void call( final Object... args )
			{
				runOnUiThread( new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText( ChatClientActivity.this, "Connected.", Toast.LENGTH_SHORT ).show();
						// サーバーに、イベント名'join' でニックネームを送信
						m_socket.emit( "join", m_strNickname );
					}
				} );
			}
		} );

		// 接続エラー時の処理
		m_socket.on( Socket.EVENT_CONNECT_ERROR, new Emitter.Listener()
		{
			@Override
			public void call( final Object... args )
			{
				runOnUiThread( new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText( ChatClientActivity.this, "Connection error.", Toast.LENGTH_SHORT ).show();
						finish();    // アクティビティ終了
						return;
					}
				} );
			}
		} );

		// 接続タイムアウト時の処理
		m_socket.on( Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener()
		{
			@Override
			public void call( final Object... args )
			{
				runOnUiThread( new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText( ChatClientActivity.this, "Connection timeout.", Toast.LENGTH_SHORT ).show();
						finish();    // アクティビティ終了
					}
				} );
			}
		} );

		// 切断時の処理
		m_socket.on( Socket.EVENT_DISCONNECT, new Emitter.Listener()
		{
			@Override
			public void call( final Object... args )
			{
				runOnUiThread( new Runnable()
				{
					@Override
					public void run()
					{
						Toast.makeText( ChatClientActivity.this, "Disconnected.", Toast.LENGTH_SHORT ).show();
					}
				} );
			}
		} );

		// 「Send」ボタンを押したときの処理
		m_edittextMessage = (EditText)findViewById( R.id.edittext_message );
		Button buttonSend = (Button)findViewById( R.id.button_send );
		buttonSend.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				String strMessage = m_edittextMessage.getText().toString();
				if( !strMessage.isEmpty() )
				{
					// サーバーに、イベント名'new message' で入力テキストを送信
					m_socket.emit( "new message", strMessage );

					m_edittextMessage.setText( "" );   // テキストボックスを空に。
				}
			}
		} );

		// メッセージを受信したときの処理
		// ・サーバー側のメッセージ拡散時の「io.emit( 'spread message', strMessage );」に対する処理
		m_messagelistadapter = new MessageListAdapter( this ); // ビューアダプターの初期化
		ListView listView = (ListView)findViewById( R.id.listview_messagelist );    // リストビューの取得
		listView.setAdapter( m_messagelistadapter );    // リストビューにビューアダプターをセット
		m_socket.on( "spread message", new Emitter.Listener()
		{
			@Override
			public void call( final Object... args )
			{
				runOnUiThread( new Runnable()
				{
					@Override
					public void run()
					{
						String strNickname = "";
						String strMessage  = "";
						String strDate     = "";

						JSONObject objMessage = (JSONObject)args[0];
						try
						{
							strNickname = objMessage.getString( "strNickname" );
							strMessage = objMessage.getString( "strMessage" );
							strDate = objMessage.getString( "strDate" );
						}
						catch( JSONException e )
						{
							e.printStackTrace();
						}

						// 拡散されたメッセージをメッセージリストに追加
						Message message = new Message( strNickname, strMessage, strDate );
						m_messagelistadapter.addMessage( message );
					}
				} );
			}
		} );
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		m_socket.disconnect();        // 切断
	}
}

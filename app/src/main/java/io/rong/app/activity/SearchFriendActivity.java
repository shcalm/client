package io.rong.app.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.adapter.SearchFriendAdapter;
import io.rong.app.database.UserInfos;
import io.rong.app.model.ApiResult;
import io.rong.app.model.Friends;
import io.rong.app.model.User1;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.utils.Constants;
import io.rong.imlib.model.UserInfo;

import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchFriendActivity extends BaseApiActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

	private final static String TAG = "SearchFriendActivity";
    private EditText mEtSearch;
    private Button mBtSearch;
    private ListView mListSearch;
    private AbstractHttpRequest<Friends> searchHttpRequest;
    private List<User1> mResultList;
    private SearchFriendAdapter adapter;
    private LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_search);
        initView();
        initData();

    }


    protected void initView() {
        getSupportActionBar().setTitle(R.string.public_account_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mEtSearch = (EditText) findViewById(R.id.de_ui_search);
        mBtSearch = (Button) findViewById(R.id.de_search);
        mListSearch = (ListView) findViewById(R.id.de_search_list);
        mResultList = new ArrayList<>();
        mDialog = new LoadingDialog(this);

    }

    protected void initData() {
        mBtSearch.setOnClickListener(this);
        mListSearch.setOnItemClickListener(this);
    }
    
    private class SearchUserTask extends AsyncTask<Void, Void, String> {

    	@Override
		protected String doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/users/search");
			
			String userName = mEtSearch.getText().toString();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("name", userName));
	     

			String result = null;
			try {
				String md5 = LoginActivity.password;
				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d(TAG, "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
				httpPost.setHeader("Authorization", "Basic " + encoding);
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				HttpResponse response = client.execute(httpPost);
				Log.d(TAG, "searchuser result code = " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {
					result = EntityUtils.toString(response.getEntity());
					Log.d(TAG, "searchuser result = " + result);
					return result;
				} else {
					return null;
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final String str) {
			if (mDialog != null)
                mDialog.dismiss();
			if (str != null) {
	            if (mResultList.size() > 0)
	                mResultList.clear();
				try {
					/** 把json字符串转换成json对象 **/
					JSONObject jsonObject = new JSONObject(str);
					String resultCode = jsonObject.getString("status");
					if (resultCode.equalsIgnoreCase("200")) {
						String id = jsonObject.getString("id");
						String name = jsonObject.getString("name");
						String portrait = jsonObject.getString("portrait");
						mResultList.add(new User1(id, name, portrait));
					} else {
						Toast.makeText(SearchFriendActivity.this,"not found", Toast.LENGTH_LONG).show();
					}

				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				adapter = new SearchFriendAdapter(mResultList, SearchFriendActivity.this);
                mListSearch.setAdapter(adapter);
                adapter.notifyDataSetChanged();

			}
	    }
	}

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        Log.e("", "------onCallApiSuccess-user.getCode() == 200)--=======---" );
       /* if (searchHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            if (mResultList.size() > 0)
                mResultList.clear();
            if (obj instanceof Friends) {
                final Friends friends = (Friends) obj;

                if (friends.getCode() == 200) {
                    if (friends.getResult().size() > 0) {
                        for (int i = 0; i < friends.getResult().size(); i++) {
                            mResultList.add(friends.getResult().get(i));
                            Log.e("", "------onCallApiSuccess-user.getCode() == 200)-----" + friends.getResult().get(0).getId().toString());
                        }
                            adapter = new SearchFriendAdapter(mResultList, SearchFriendActivity.this);
                            mListSearch.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                    }

                }
            }
        }*/

    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (searchHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            Log.e("", "------onCallApiSuccess-user.============onCallApiFailure()--");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mBtSearch)) {
            String userName = mEtSearch.getText().toString();
            if (DemoContext.getInstance() != null) {
                //searchHttpRequest = DemoContext.getInstance().getDemoApi().searchUserByUserName(userName, this);
            	SearchUserTask task = new SearchUserTask();
            	task.execute();
            }

            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.PERSONAL_REQUESTCODE) {
            Intent intent = new Intent();
            this.setResult(Constants.SEARCH_REQUESTCODE, intent);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent in = new Intent(this, DePersonalDetailActivity.class);

        in.putExtra("SEARCH_USERID", mResultList.get(position).getId());
        in.putExtra("SEARCH_USERNAME", mResultList.get(position).getName());
        in.putExtra("SEARCH_PORTRAIT", mResultList.get(position).getPortrait());
        startActivityForResult(in, Constants.SEARCH_REQUESTCODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}

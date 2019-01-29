package com.example.hasith.canu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.example.hasith.canu.models.jobCardModel;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class jobCardList extends AppCompatActivity {

    private final String URL_TO_HIT = "https://api.myjson.com/bins/8u6a8";
    private TextView tvData;
    private ListView jobList;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_card_list);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading. Please wait...");

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
//        ImageLoader.getInstance().init(config);

        jobList = (ListView)findViewById(R.id.lvMovies);

        new JSONTask().execute(URL_TO_HIT);
    }

    public class JSONTask extends AsyncTask<String,String, List<jobCardModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected List<jobCardModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("movies");

                List<jobCardModel> movieModelList = new ArrayList<>();

                Gson gson = new Gson();
                for(int i=0; i<parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    /**
                     * below single line of code from Gson saves you from writing the json parsing yourself
                     * which is commented below
                     */
                    jobCardModel movieModel = gson.fromJson(finalObject.toString(), jobCardModel.class); // a single line json parsing using Gson
//
                    movieModelList.add(movieModel);
                }
                return movieModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return  null;
        }

        @Override
        protected void onPostExecute(final List<jobCardModel> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result != null) {
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.row, result);
                jobList.setAdapter(adapter);
                jobList.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // list item click opens a new detailed activity
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        jobCardModel movieModel = result.get(position); // getting the model
                        Intent intent = new Intent(jobCardList.this, jobCardView.class);
                        intent.putExtra("movieModel", new Gson().toJson(movieModel)); // converting model json into string type and sending it via intent
                        startActivity(intent);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Not able to fetch data from server, please check url.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new JSONTask().execute(URL_TO_HIT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MovieAdapter extends ArrayAdapter {

        private List<jobCardModel> movieModelList;
        private int resource;
        private LayoutInflater inflater;
        public MovieAdapter(Context context, int resource, List<jobCardModel> objects) {
            super(context, resource, objects);
            movieModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if(convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.MachineImage= (ImageView)convertView.findViewById(R.id.ivIcon);
                holder.tvFault = (TextView)convertView.findViewById(R.id.tvFault);
                holder.tvStatus = (TextView)convertView.findViewById(R.id.tvStatus);
                holder.tvSerial_number = (TextView)convertView.findViewById(R.id.tvSerial_number);
                holder.tvDepartment = (TextView)convertView.findViewById(R.id.tvDepartment);
                holder.tvDate = (TextView)convertView.findViewById(R.id.tvDate);

                //holder.tvTechnicians = (TextView)convertView.findViewById(R.id.tvTechnicians);
                holder.tvDescription= (TextView)convertView.findViewById(R.id.tvDescription);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            // Then later, when want to display image
            final ViewHolder finalHolder = holder;
//            ImageLoader.getInstance().displayImage(movieModelList.get(position).getImage(), holder.MachineImage, new ImageLoadingListener() {
//                @Override
//                public void onLoadingStarted(String imageUri, View view) {
//                    progressBar.setVisibility(View.VISIBLE);
//                    finalHolder.MachineImage.setVisibility(View.INVISIBLE);
//                }
//
//                @Override
//                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                    progressBar.setVisibility(View.GONE);
//                    finalHolder.MachineImage.setVisibility(View.INVISIBLE);
//                }
//
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    progressBar.setVisibility(View.GONE);
//                    finalHolder.MachineImage.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onLoadingCancelled(String imageUri, View view) {
//                    progressBar.setVisibility(View.GONE);
//                    finalHolder.MachineImage.setVisibility(View.INVISIBLE);
//                }
//            });

            holder.tvFault.setText(movieModelList.get(position).getFault());
            holder.tvStatus.setText(movieModelList.get(position).getStatus());
            holder.tvSerial_number.setText("Serial_number: " + movieModelList.get(position).getSerial_num());
            holder.tvDepartment.setText("Department:" + movieModelList.get(position).getDepartment());
            holder.tvDate.setText("Date:" + movieModelList.get(position).getDate());

            // rating bar
            // holder.rbMovieRating.setRating(movieModelList.get(position).getRating()/2);

            StringBuffer stringBuffer = new StringBuffer();
            // for(MovieModel.Technicians tech: movieModelList.get(position).getTechniciansList()){
            // stringBuffer.append(tech.getName() + ", ");
            //}

            // holder.tvTechnicians.setText("Technicians:" + stringBuffer);
            holder.tvDescription.setText(movieModelList.get(position).getDescription());
            return convertView;
        }


        class ViewHolder{
            private ImageView MachineImage;
            private TextView tvFault;
            private TextView tvDescription;
            private TextView tvSerial_number;
            private TextView tvDepartment;
            private TextView tvDate;

            private TextView tvTechnicians;
            private TextView tvStatus;
        }

    }
}

package com.example.abhin.hackgt5app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.Credentials;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clarifai.api.RecognitionRequest;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import static com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance;

//import com.google.api.client.json.gson.GsonFactory;

//import static com.google.api.client.json.gson.GsonFactory.*;

import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;

public class MainActivity extends AppCompatActivity {
    ContentValues values;
    Uri imageUri;
    private static HashSet<String> recyclables = new HashSet<String>();
    private static HashSet<String> compostables = new HashSet<String>();
    private static HashSet<String> technologys = new HashSet<String>();
    private static HashSet<String> notApplicableSet = new HashSet<>();
    float[] imageSize = new float[2]; //(width,height)

    private final ClarifaiClient clarifaiClient = new ClarifaiBuilder(com.example.abhin.hackgt5app.Credentials.CLARIFAI_API_KEY).buildSync();
    //   private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    ImageView imageView;
    TextView mImageDetails;

    static final int CAM_REQUEST = 1;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        final Button btnCamera = (Button) findViewById(R.id.btnCamera);
        imageView = (ImageView) findViewById(R.id.imageView);

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    CAM_REQUEST);
        }

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    3);
        }
        if (checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET},
                    4);
        }

        mImageDetails = (TextView) findViewById(R.id.textView);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values = new ContentValues();
                // create a package provider string suggested by the error messge.
                String provider = "com.android.providers.media.MediaProvider";

                // grant all three uri permissions!
                //grantUriPermission(provider, Uri.parse("content://media/external/images/media"), Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAM_REQUEST);
            }
        });

        String[] recyclable = {"paper",
                "newspaper",
                "magazine",
                "catalog",
                "map",
                "phonebook",
                "mail",
                "paperboard",
                "tissue",
                "box",
                "card",
                "folder",
                "can",
                "straw",
                "carton",
                "book",
                "cup",
                "envelope",
                "cardboard",
                "vase",
                "plastic",
                "boxboard",
                "box",
                "metal",
                "tin",
                "aluminum",
                "dish",
                "plate",
                "tray",
                "cookware",
                "copper",
                "jewelry",
                "key",
                "steel",
                "pot",
                "bucket",
                "pan",
                "tin",
                "pyrex",
                "utensil",
                "glass",
                "bottle",
                "jar",
                "cup",
                "jug",
                "metal",
                "spoon",
                "fork",
                "office paper",
                "blind",
                "curtain", "recyclable"};
        String[] technology = {"battery",
                "computer",
                "electronics",
                "bulb",
                "microfilm",
                "cell phone",
                "phone",
                "mobile phone",
                "equipment",
                "inkjet",
                "cartridge",
                "inkjet cardridge",
                "cd",
                "disk",
                "tire",
                "ink cartridge",
                "tv",
                "power cord",
                "personal computer",
                "laptop",
                "portable computer"};
        String[] composting = {"fruit",
                "vegetable",
                "apple",
                "pear",
                "banana",
                "cucumber",
                "strawberry",
                "apricots",
                "avocado",
                "blackberry",
                "cherry",
                "coconut",
                "date",
                "durian",
                "dragonfruit",
                "grape",
                "grapefruit",
                "kiwi",
                "lime",
                "lemon",
                "lychee",
                "mango",
                "melon",
                "nectarine",
                "olive",
                "orange",
                "peach",
                "pineapple",
                "plum",
                "pomegranate",
                "pomelo",
                "raspberries",
                "watermelon",
                "broccoflower",
                "broccoli",
                "cabbage",
                "celery",
                "corn",
                "basil",
                "rosemary",
                "sage",
                "thyme",
                "kale",
                "lettuce",
                "mushroom",
                "onion",
                "pepper",
                "ginger",
                "wasabi",
                "squash",
                "tomato",
                "potato",
                "hair",
                "wood",
                "popcorn",
                "leaves",
                "egg",
                "pasta",
                "fish",
                "beef",
                "chicken",
                "pork",
                "meat",
                "soy",
                "pumpkin",
                "nut",
                "cheese",
                "toothpicks",
                "pickles",
                "feather",
                "fur",
                "bone", "compostable"};
        String[] notApplicable = {
                "person",
                "interior",
                "urban design",
                "family",
                "human",
                "ground",
                "wall",
                "door",
                "desk",
                "chair",
                "window",
                "pattern",
                "texture",
                "fabric",
                "background"
        };
        for (int i = 0; i < composting.length; i++) {
            compostables.add(composting[i]);
        }
        for (int i = 0; i < technology.length; i++) {
            technologys.add(technology[i]);
        }
        for (int i = 0; i < recyclable.length; i++) {
            recyclables.add(recyclable[i]);
        }
        for(int i = 0; i < notApplicable.length; i++) {
            notApplicableSet.add(notApplicable[i]);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.about:
                Intent about = new Intent(this, About.class);
                startActivity(about);
                break;
            case R.id.stats:
                Intent stats = new Intent(this, StatsActivity.class);
                startActivity(stats);
                break;
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                break;
            default:
                //unknown error
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAM_REQUEST) {
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                //imageurl = getRealPathFromURI(imageURI);

//            uploadImage(data.getData());
                new AsyncTask<Bitmap, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {
                    @Override
                    protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Bitmap... bitmaps) {
                        mImageDetails.setVisibility(imageView.INVISIBLE);
                        progressBar.setVisibility(1);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        byte[] byteArray = stream.toByteArray();
                        return clarifaiClient.getDefaultModels().generalModel().predict().withInputs(ClarifaiInput.forImage(byteArray)).executeSync();
//                    return clarifaiClient.(new RecognitionRequest(byteArray).setModel("general-v1.3")).get(0);

                    }

                    @Override
                    protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> result) {
                        boolean found = false;
                        String foundText = "Trash";

                        for (Concept tag : result.getOrNull().get(0).data()) {
                            System.out.println(
                                    tag.name());

                            mImageDetails.setVisibility(1);
                            mImageDetails.setText("Processing...");

                            if (recyclables.contains(tag.name())) {
                                foundText = "Recyclable Item";
//                            mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color.pukeGreen, null));
                                found = true;
                            } else if (compostables.contains(tag.name())) {
                                foundText = "Compostable Item";
//                            mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color.blue, null));
                                found = true;
                            } else if (technologys.contains(tag.name())) {
                                foundText = "Technology";
//                            mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color.heartRed, null));
                                found = true;
                            } else if (notApplicableSet.contains(tag.name())) {
                                foundText = "Not Applicable";
                            }
                        }
                        if (!found) {
                            foundText = "Trash";
//                        mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color., null));
                        }
                       mImageDetails.setText(foundText);
                        progressBar.setVisibility(imageView.INVISIBLE);

                    }
                }.execute(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //imageSize[0] = bitmap.getWidth();
            //imageSize[1] = bitmap.getHeight();
        }


//        if (data != null && resultCode == RESULT_OK && requestCode == CAM_REQUEST) {
//            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(bitmap);
////            uploadImage(data.getData());
//            new AsyncTask<Bitmap, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {
//                @Override
//                protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Bitmap... bitmaps) {
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 90, stream);
//                    byte[] byteArray = stream.toByteArray();
//                    return clarifaiClient.getDefaultModels().generalModel().predict().withInputs(ClarifaiInput.forImage(byteArray)).executeSync();
////                    return clarifaiClient.(new RecognitionRequest(byteArray).setModel("general-v1.3")).get(0);
//                }
//
//                @Override
//                protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> result) {
//                    boolean found = false;
//                    String foundText = "Trash :(";
//                    HashSet<String> recyclables = new HashSet<String>();
//                    HashSet<String> compostables = new HashSet<String>();
//                    HashSet<String> technogy = new HashSet<String>();
//                    for (Concept tag : result.getOrNull().get(0).data()) {
//                        System.out.println(
//                                tag.name());
//                        if (recyclables.contains(tag.name())) {
//                            foundText = "Recycle! :)";
////                            mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color.pukeGreen, null));
//                            found = true;
//                        } else if (compostables.contains(tag.name())) {
//                            foundText = "Compost! :)";
////                            mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color.blue, null));
//                            found = true;
//                        } else if (technologys.contains(tag.name())) {
//                            foundText = "Trash! :(";
////                            mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color.heartRed, null));
//                            found = true;
//                        }
//                    }
//                    if (!found) {
//                        foundText = "Trash! :(";
////                        mImageDetails.setTextColor(ResourcesCompat.getColor(getResources(), R.color., null));
//                    }
//                    mImageDetails.setText(foundText);
//                }
//            }.execute(bitmap);
//        }


//        else if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            uploadImage(data.getData());
//        }
//        else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
//            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider",
//                    getCameraFile());
//            uploadImage(photoUri);
//        }
    }
}
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            uploadImage(data.getData());
//        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
//            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
//            uploadImage(photoUri);
//        }
//    }
    /*
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(final Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer( "AIzaSyCLycmtn14lGB1PTUOc_oolZlt5GJY_nGk ") { // CLOUD_VISION_API_KEY
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
    /*
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        BatchAnnotateImagesRequest label_detection = batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("LABEL_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.mImageDetails);
                imageDetail.setText(result);
            }
        }
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("I found these things:\n\n");

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }*/

// }
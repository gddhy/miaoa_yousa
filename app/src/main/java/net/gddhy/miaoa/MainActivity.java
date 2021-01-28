package net.gddhy.miaoa;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    LinearLayout linearLayout;
    WebView webView;
    final String link = "https://gddhy.net/2020/miaoa-yousa/";
    final String cdn_link = "https://cdn.jsdelivr.net/gh/gddhy/gddhy.github.io/2020/miaoa-yousa/";
    ProgressDialog progressDialog;
    LinearLayout local;
    LinearLayout main;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.main_WebView);
        linearLayout =findViewById(R.id.main_LinearLayout);
        local = findViewById(R.id.local);
        main = findViewById(R.id.main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中");
        progressDialog.setCancelable(false);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,String url){
                if(url.contains(link) && url.contains("@")){
                    String type = url.substring(url.lastIndexOf("@")+1);
                    if(type.contains("/")){
                        type = type.substring(0,type.lastIndexOf("/"));
                    }
                    switch (type){
                        case "onWhiteListSetting":
                            onWhiteListSetting(webView);
                            break;
                        case "onACTION_BATTERY_OPTIMIZATIONS":
                            onACTION_BATTERY_OPTIMIZATIONS(webView);
                            break;
                        case "toSelfSetting":
                            onAppSetting(webView);
                            break;
                        case "onCustomize":
                            onCustomize(webView);
                            break;
                        case "onPlaySound":
                            onPlaySound(webView);
                            break;
                        case "onDefault":
                            onDefault(webView);
                            break;
                        default:
                    }
                } else if(url.contains(link) || url.contains(cdn_link) && url.contains("?")){
                    progressDialog.show();
                    String type = url.substring(url.lastIndexOf("?")+1);
                    String file = url.substring(0,url.lastIndexOf("?"));
                    downFile(file,type.contains("set"));
                } else if(url.contains("gddhy.net")) {
                    view.loadUrl(url);
                } else {
                    openUrl(url);
                }
                return true;
            }


            //https://blog.csdn.net/haha223545/article/details/74566408
            private boolean isSuccess = false;
            private boolean isError = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isError) {
                    isSuccess = true;
                    //回调成功后的相关操作
                    linearLayout.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    //某些时候无法跳转到more标签，多加载一次
                    webView.loadUrl(link+"#more");
                }
                isError = false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                isError = true;
                isSuccess = false;
                //回调失败的相关操作
            }
        });
        webView.loadUrl(link+"#more");

    }



    @TargetApi(Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }
    //https://blog.csdn.net/zhanglei892721/article/details/103909769
    @TargetApi(Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,10086);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 10086:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this,"设置成功",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this,"设置失败",Toast.LENGTH_LONG).show();
                }
                break;

            case 10000:
                if (resultCode == Activity.RESULT_OK) {
                    if(saveFileFromSAF(this,data.getData())){
                        Toast.makeText(this,"设置成功",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "设置成功", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this,"未选择文件",Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void toSelfSetting(Context context) {
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        mIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(mIntent);
    }

    public void onWhiteListSetting(View view) {
        //UWhiteListSetting.enterWhiteListSetting(this);
        Util.startToAutoStartSetting(this);
    }

    public void onACTION_BATTERY_OPTIMIZATIONS(View view) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(!isIgnoringBatteryOptimizations()){
                requestIgnoreBatteryOptimizations();
            } else {
                Toast.makeText(this,"已设置，无需重复设置",Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,"当前设备系统低于Android6\n无需执行此操作",Toast.LENGTH_LONG).show();
        }
    }

    public void onAppSetting(View view) {
        toSelfSetting(this);
    }

    public void onCustomize(View view) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            startActivityForResult(intent, 10000);
        } else {
            Toast.makeText(this,"当前系统版本过低，不支持该操作",Toast.LENGTH_LONG).show();
        }
    }

    public void onPlaySound(View view) {
        Intent intent = new Intent(this,SoundPlayService.class);
        startService(intent);
    }

    public void onDefault(View view) {
        File file = new File(getFilesDir(),"music.mp3");
        if(file.exists()){
            if(file.delete()){
                Toast.makeText(this,"已恢复默认",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,"操作失败，请重试",Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,"未使用自定义音频文件",Toast.LENGTH_LONG).show();
        }
    }


    public static boolean saveFileFromSAF(Context context,Uri safUri){
        DocumentFile documentFile = DocumentFile.fromSingleUri(context,safUri);
        try {
            File save = new File(context.getFilesDir(),"music.mp3");
            if(save.exists()){
                save.delete();
            }
            if(!save.exists()){
                save.createNewFile();
            }
            InputStream in  = context.getContentResolver().openInputStream(documentFile.getUri());
            FileOutputStream out = new FileOutputStream(save);
            int n = 0;// 每次读取的字节长度
            byte[] bb = new byte[1024];// 存储每次读取的内容
            while ((n = in.read(bb)) != -1) {
                out.write(bb, 0, n);// 将读取的内容，写入到输出流当中
            }
            in.close();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void openUrl(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }



    private void downFile(final String file_Url, final boolean isSet){
        final File tmpFile = new File(getCacheDir(),"tmp.mp3");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(tmpFile.exists()){
                        tmpFile.delete();
                    }
                    downLoadFromUrl(file_Url,tmpFile);
                    if(isSet){
                        File file = new File(getFilesDir(),"music.mp3");
                        copyFile(tmpFile.getPath(),file.getPath());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this,"已设置",Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                        MediaPlayer.create(MainActivity.this, Uri.fromFile(tmpFile)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,(isSet?"设置":"播放")+"失败",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


    /**
     * https://blog.csdn.net/xb12369/article/details/40543649
     * 有修改
     * 从网络Url中下载文件
     * @param urlStr
     * @param saveFile
     * @throws IOException
     */
    public static void  downLoadFromUrl(String urlStr,File saveFile) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
        if(!saveFile.getParentFile().exists()){
            saveFile.getParentFile().mkdir();
        }
        FileOutputStream fos = new FileOutputStream(saveFile);
        fos.write(getData);
        if(fos!=null){
            fos.close();
        }
        if(inputStream!=null){
            inputStream.close();
        }

    }



    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     *  复制单个文件
     *  @param  oldPath  String  原文件路径  如：c:/fqf.txt
     *  @param  newPath  String  复制后路径  如：f:/fqf.txt
     *  @return  boolean
     */
    public static void  copyFile(String  oldPath,  String  newPath)  {
        try  {
//           int  bytesum  =  0;
            int  byteread  =  0;
            File  oldfile  =  new  File(oldPath);
            if  (oldfile.exists())  {
                InputStream  inStream  =  new FileInputStream(oldPath);
                FileOutputStream  fs  =  new  FileOutputStream(newPath);
                byte[]  buffer  =  new  byte[1444];
                while  (  (byteread  =  inStream.read(buffer))  !=  -1)  {
                    fs.write(buffer,  0,  byteread);
                }
                inStream.close();
            }
        }
        catch  (Exception  e)  {
            e.printStackTrace();
        }
    }

    public void onOnline(View view) {
        linearLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    public void onLocal(View view) {
        webView.setVisibility(View.GONE);
        main.setVisibility(View.GONE);
        local.setVisibility(View.VISIBLE);
    }
}

package cn.skyui.module.ugc.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.sdk.android.oss.ServiceException;
import com.amap.api.location.AMapLocation;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chenenyu.router.annotation.Route;
import com.orhanobut.logger.Logger;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.skyui.library.base.activity.BaseActivity;
import cn.skyui.library.data.model.User;
import cn.skyui.library.event.ugc.UgcEvent;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.library.image.picker.ImagePicker;
import cn.skyui.library.image.picker.activity.ImageGridActivity;
import cn.skyui.library.image.picker.bean.ImageItem;
import cn.skyui.library.image.picker.loader.GlideImageLoader;
import cn.skyui.library.utils.KeyboardUtils;
import cn.skyui.library.utils.LocationUtils;
import cn.skyui.library.utils.SizeUtils;
import cn.skyui.library.utils.StringUtils;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.library.utils.oss.UploadCallbackHandler;
import cn.skyui.library.utils.oss.UploadClient;
import cn.skyui.library.widget.progress.ProgressDialog;
import cn.skyui.library.widget.recyclerview.GridSpacingItemDecoration;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.data.constant.Constants;
import cn.skyui.module.ugc.data.model.ugc.PhotoItem;
import cn.skyui.module.ugc.R;
import de.greenrobot.event.EventBus;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by tiansj on 2018/4/14.
 */
@Route("publish")
public class PublishActivity extends BaseActivity {

    private static final int IMAGE_PICKER_RESULT_CODE = 1;

    private EditText mTextTitle;
    private TextView mTextPhotoCount;
    private TextView mTextContentTips;
    private RecyclerView mRecyclerView;
    private ImageView mImgLocation;
    private TextView mTextLocation;
    private ImageView mImgLocationClose;

    private ArrayList<ImageItem> imageItems;
    private List<PhotoItem> photoItems;
    private MultipleItemQuickAdapter multipleItemQuickAdapter;
    private AMapLocation userLocation;
    private RelativeLayout mLayoutLocation;
    private boolean isOpenLocation = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_photo);

        initData();
        initView();
        autoLocation();
    }

    private void initData() {
        imageItems = new ArrayList<>();
        photoItems = new ArrayList<>();
        PhotoItem addPhotoItem = new PhotoItem(PhotoItem.ADD);
        photoItems.add(addPhotoItem);
    }

    private void initView() {
        initImagePicker();
        mTextTitle = findViewById(R.id.textTitle);
        mTextPhotoCount = findViewById(R.id.textPhotoCount);
        mTextContentTips = findViewById(R.id.textContentTips);
        mImgLocation = findViewById(R.id.imgLocation);
        mTextLocation = findViewById(R.id.textLocation);

        mTextTitle.addTextChangedListener(mTextWatcher);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, SizeUtils.dp2px(2f), false));

        multipleItemQuickAdapter = new MultipleItemQuickAdapter(photoItems);
        multipleItemQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        multipleItemQuickAdapter.setOnItemClickListener((adapter, view, position) -> {
            PhotoItem photoItem = (PhotoItem) adapter.getItem(position);
            if (photoItem == null) {
                return;
            }
            if (photoItem.getItemType() == PhotoItem.ADD) {
                Intent intent = new Intent(this, ImageGridActivity.class);
                intent.putExtra(ImageGridActivity.EXTRAS_IMAGES, imageItems);
                startActivityForResult(intent, IMAGE_PICKER_RESULT_CODE);
            }
        });
        mRecyclerView.setAdapter(multipleItemQuickAdapter);
        mImgLocationClose = findViewById(R.id.imgLocationClose);
        mLayoutLocation = findViewById(R.id.layoutLocation);

        mImgLocationClose.setOnClickListener(v -> {
            ToastUtils.showShort("本次定位已关闭");
            mLayoutLocation.setVisibility(View.GONE);
            isOpenLocation = false;
        });
    }

    @SuppressLint("CheckResult")
    private void autoLocation() {
        new RxPermissions(mActivity).request(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(permission -> {
                    if (!permission) {
                        ToastUtils.showShort("授定位权限，可以让更多人认识你噢");
                    } else {
                        LocationUtils.getInstance().startLocation(new LocationUtils.OnLocationChangedListener() {
                            @Override
                            public void onSuccess(AMapLocation mapLocation) {
                                Logger.i("location success");
                                mTextLocation.setText(mapLocation.getCity() + " ∙ " + mapLocation.getStreet());
                                userLocation = mapLocation;
                            }

                            @Override
                            public void onFail(int errCode, String errInfo) {
                                Logger.e("location failed");
                                mTextLocation.setText("发布位置，可以让更多人认识你噢");
                            }
                        });
                    }
                });
    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setMultiMode(true);
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setSelectLimit(9);    //选中数量限制
    }

    private static final int MAX_SIZE = 100;

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int left = MAX_SIZE - StringUtils.length(s.toString());
            mTextContentTips.setText("您还可以输入" + left + "个字");
        }
    };

    public class MultipleItemQuickAdapter extends BaseMultiItemQuickAdapter<PhotoItem, BaseViewHolder> {

        public MultipleItemQuickAdapter(List<PhotoItem> data) {
            super(data);
            addItemType(PhotoItem.ADD, R.layout.layout_publish_photo_item_add);
            addItemType(PhotoItem.IMG, R.layout.layout_publish_photo_item);
        }

        @Override
        protected void convert(BaseViewHolder helper, PhotoItem item) {
            switch (helper.getItemViewType()) {
                case PhotoItem.ADD:
                    break;
                case PhotoItem.IMG:
                    GlideApp.with(mActivity).load(item.getImagePath()).into((ImageView) helper.getView(R.id.imageView));
                    helper.getView(R.id.imgDelete).setOnClickListener(v -> {
                        multipleItemQuickAdapter.remove(helper.getAdapterPosition());

                        if (imageItems.size() == 9) {
                            multipleItemQuickAdapter.addData(new PhotoItem(PhotoItem.ADD));
                        }

                        for (ImageItem imageItem : imageItems) {
                            if (imageItem.path.equals(item.getImagePath())) {
                                imageItems.remove(imageItem);
                                break;
                            }
                        }

                        int count = imageItems != null ? imageItems.size() : 0;
                        mTextPhotoCount.setText(count + " / " + 9);
                    });
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER_RESULT_CODE) {
                imageItems = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);

                photoItems.clear();
                if (imageItems != null && imageItems.size() > 0) {
                    for (ImageItem imageItem : imageItems) {
                        PhotoItem photoItem = new PhotoItem(PhotoItem.IMG);
                        photoItem.setImagePath(imageItem.path);
                        photoItems.add(photoItem);
                    }
                }
                if (imageItems == null || imageItems.size() < 9) {
                    PhotoItem addPhotoItem = new PhotoItem(PhotoItem.ADD);
                    photoItems.add(addPhotoItem);
                }
                multipleItemQuickAdapter.replaceData(photoItems);

                int count = imageItems != null ? imageItems.size() : 0;
                mTextPhotoCount.setText(count + " / " + 9);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {
            KeyboardUtils.hideSoftInput(this);
            save();
        }
        return super.onOptionsItemSelected(item);
    }

    private HashMap<String, String> path2urlMap = new HashMap<>();
    private ProgressDialog progressDialog;

    private void save() {
        if (imageItems == null || imageItems.size() == 0) {
            ToastUtils.showShort("至少选择一张图片");
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setLoadingMessage("正在保存");
        progressDialog.show();
        try {
            upload(imageItems.get(0).path, 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void upload(final String path, final int fileIndex) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            ToastUtils.showShort("找不到图片");
            return;
        }

        if (path2urlMap.containsKey(path) && fileIndex < (imageItems.size() - 1)) {
            ImageItem imageItem = imageItems.get(fileIndex + 1);
            try {
                upload(imageItem.path, fileIndex + 1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }


        UploadClient.asyncUpload(file, new UploadCallbackHandler() {
            @Override
            public void onSuccess(String objectKey) {
                String url = UploadClient.BASE_IMAGE_DOMAIN + objectKey;
                path2urlMap.put(path, url);
                Logger.d("[onSuccess] - " + url + " upload success!");

                if (fileIndex < (imageItems.size() - 1)) {
                    ImageItem imageItem = imageItems.get(fileIndex + 1);
                    try {
                        upload(imageItem.path, fileIndex + 1);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    httpSaveUgc();
                }
            }

            @Override
            public void onProgress(String objectKey, long byteCount, long totalSize) {
            }

            @Override
            public void onFailure(String objectKey, ServiceException ossException) {
                Logger.d("[onFailure] - upload " + objectKey + " failed!\n" + ossException.toString());
                ToastUtils.showShort("上传图片失败，请重新提交");
                progressDialog.dismiss();
            }
        });
    }

    private void httpSaveUgc() {
        JSONArray jsonArray = new JSONArray();
        Logger.i("imageItems=" + JSONArray.toJSON(imageItems));
        for (ImageItem imageItem : imageItems) {
            jsonArray.add(path2urlMap.get(imageItem.path));
        }
        String title = mTextTitle.getText().toString();
        Logger.i("title=" + title);
        Logger.i("images=" + jsonArray.toJSONString());
        String location = null;
        if (isOpenLocation && userLocation != null) {
            location = Base64.encodeToString(userLocation.toStr().getBytes(), Base64.DEFAULT);
        }
//        Map<String, String> fields = new HashMap<>();
//        fields.put("type", String.valueOf(Constants.UGC.TYPE_IMAGE));
//        fields.put("title", title);
//        fields.put("images", jsonArray.toJSONString());
//        fields.put("location", location);

        RetrofitFactory.createService(ApiService.class)
//                .saveUgc(fields)
                .saveUgc(Constants.UGC.TYPE_IMAGE, title, jsonArray.toJSONString(), location)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<Long>() {
                    @Override
                    protected void onSuccess(Long response) {
                        ToastUtils.showShort("动态发布成功");
                        int ugcCount = User.getInstance().detail.getUser().getUgcCount();
                        User.getInstance().detail.getUser().setUgcCount(ugcCount + 1);
                        EventBus.getDefault().post(new UgcEvent.PublishSuccess());
                        progressDialog.dismiss();
                        mActivity.finish();
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        ToastUtils.showShort("发布失败，请重新提交");
                        progressDialog.dismiss();
                    }
                });
    }
}
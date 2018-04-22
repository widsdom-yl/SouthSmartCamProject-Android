package stcam.stcamproject.network.api;

import com.model.LampStateModel;
import com.model.RecordConfigModel;
import com.model.RetModel;
import com.model.SDImageModel;
import com.model.SDVideoModel;
import com.model.SSIDModel;
import com.model.SSIDSavedModel;
import com.model.SnapShotConfigModel;
import com.model.WIFIStateModel;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by GYL on 11/22/16.
 */

public interface CommandApi {


    /**
     * ֻתΪ AP�����޸�
     * @param User
     * @param admin
     * @param MsgID 38
     * @param wifi_Active 1
     * @param wifi_IsAPMode 1
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> STA2AP_keepValue(@Query("User") String User,
                                          @Query("Psd") String admin,
                                          @Query("MsgID") int MsgID,
                                          @Query("wifi_Active") int wifi_Active,
                                          @Query("wifi_IsAPMode") int wifi_IsAPMode,
                                          @Query("s") int s
    );

    /**
     * ֻתΪ AP�����޸�
     * @param User
     * @param admin
     * @param MsgID 38
     * @param wifi_Active 1
     * @param wifi_IsAPMode 1
     * @param wifi_SSID_AP
     * @param wifi_Password_AP
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> STA2AP_changeValue(@Query("User") String User,
                                            @Query("Psd") String admin,
                                            @Query("MsgID") int MsgID,
                                            @Query("wifi_Active") int wifi_Active,
                                            @Query("wifi_IsAPMode") int wifi_IsAPMode,
                                            @Query("wifi_SSID_AP") String wifi_SSID_AP,
                                            @Query("wifi_Password_AP") String wifi_Password_AP,
                                            @Query("s") int s
    );


    /**
     *
     * @param User
     * @param admin
     * @param MsgID 38
     * @param wifi_Active 1
     * @param wifi_IsAPMode 0
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> AP2STA_keepValue(@Query("User") String User,
                                          @Query("Psd") String admin,
                                          @Query("MsgID") int MsgID,
                                          @Query("wifi_Active") int wifi_Active,
                                          @Query("wifi_IsAPMode") int wifi_IsAPMode,
                                          @Query("s") int s
    );

    /**
     *
     * @param User
     * @param admin
     * @param MsgID 38
     * @param wifi_Active 1
     * @param wifi_IsAPMode 0
     * @param wifi_SSID_AP
     * @param wifi_Password_AP
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> AP2STA_changeValue(@Query("User") String User,
                                            @Query("Psd") String admin,
                                            @Query("MsgID") int MsgID,
                                            @Query("wifi_Active") int wifi_Active,
                                            @Query("wifi_IsAPMode") int wifi_IsAPMode,
                                            @Query("wifi_SSID_STA") String wifi_SSID_AP,
                                            @Query("wifi_Password_STA") String wifi_Password_AP,
                                            @Query("s") int s
    );

    /**
     * MsgID
     * ����  20
     * ¼��
     * ��ʼ¼�� 21
     * ֹͣ¼�� 22
     * �Ƿ�¼�� 85
     *
     */

    @GET("cfg1.cgi")
    Observable<RetModel> controlMedia(@Query("User") String User,
                                      @Query("Psd") String admin,
                                      @Query("MsgID") int MsgID,
                                      @Query("Chl") int Chl,
                                      @Query("s") int s
    );


    /**
     * �����豸ʱ��
     * @param User
     * @param admin
     * @param MsgID 17
     * @param time
     * @return
     */

    @GET("cfg1.cgi")
    Observable<RetModel> updateTime(@Query("User") String User,
                                    @Query("Psd") String admin,
                                    @Query("MsgID") int MsgID,
                                    @Query("time") String time,
                                    @Query("s") int s
    );

    /**
     *
     * @param User
     * @param admin
     * @param MsgID 91
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> getRecordMediaState(@Query("User") String User,
                                             @Query("Psd") String admin,
                                             @Query("MsgID") int MsgID,
                                             @Query("s") int s
    );

    /**
     * ����
     * @param User
     * @param admin
     * @param MsgID 18
     * @return
     */

    @GET("cfg1.cgi")
    Observable<RetModel> reboot(@Query("User") String User,
                                @Query("Psd") String admin,
                                @Query("MsgID") int MsgID,
                                @Query("s") int s
    );

    /**
     * ��ȡ��Ƭ���
     * @param User
     * @param admin
     * @param MsgID 86
     * @param p
     * @param l
     * @return
     */
    @GET("cfg1.cgi")
    Observable<List<SDImageModel>> getImageList(@Query("User") String User,
                                                @Query("Psd") String admin,
                                                @Query("MsgID") int MsgID,
                                                @Query("p") int p,
                                                @Query("l") int l,
                                                @Query("s") int s
    );

    /**
     * ��ȡ¼���б�
     * @param User
     * @param admin
     * @param MsgID 5
     * @param p
     * @param l
     * @return
     */
    @GET("cfg1.cgi")
    Observable<List<SDVideoModel>> getVideoList(@Query("User") String User,
                                                @Query("Psd") String admin,
                                                @Query("MsgID") int MsgID,
                                                @Query("p") int p,
                                                @Query("l") int l,
                                                @Query("s") int s
    );

    /**
     * ɾ���ļ�
     * @param User
     * @param admin
     * @param MsgID 77
     * @param FileName
     * @return
     */
    @GET("cfg1.cgi")
    Observable<List<RetModel>> deleteFile(@Query("User") String User,
                                          @Query("Psd") String admin,
                                          @Query("MsgID") int MsgID,
                                          @Query("FileName") String FileName,
                                          @Query("s") int s
    );

    /**
     * ��ȡLED��״̬
     * @param User
     * @param admin
     * @param MsgID
     * @return
     */
    @GET("cfg1.cgi")
    Observable<LampStateModel> getLampState(@Query("User") String User,
                                            @Query("Psd") String admin,
                                            @Query("MsgID") int MsgID,
                                            @Query("s") int s
    );

    /**
     * ���ÿ��ص�
     * @param User
     * @param admin
     * @param MsgID
     * @param Chl
     * @param Value
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> setLampState(@Query("User") String User,
                                      @Query("Psd") String admin,
                                      @Query("MsgID") int MsgID,
                                      @Query("Chl") int Chl,
                                      @Query("Value") int Value,
                                      @Query("s") int s
    );

    /**
     * ��ȡ��������
     * @param User
     * @param admin
     * @param MsgID 87
     * @return
     */
    @GET("cfg1.cgi")
    Observable<SnapShotConfigModel> getSnapShotConfig(@Query("User") String User,
                                                      @Query("Psd") String admin,
                                                      @Query("MsgID") int MsgID,
                                                      @Query("s") int s
    );

    /**
     * ��ȡ¼������
     * @param User
     * @param admin
     * @param MsgID 89
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RecordConfigModel> getRecordConfig(@Query("User") String User,
                                                  @Query("Psd") String admin,
                                                  @Query("MsgID") int MsgID,
                                                  @Query("s") int s
    );

    /**
     * ����¼������
     * @param User
     * @param admin
     * @param MsgID 90
     * @param TotalTime
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> setRecordConfig(@Query("User") String User,
                                         @Query("Psd") String admin,
                                         @Query("MsgID") int MsgID,
                                         @Query("TotalTime") int TotalTime,
                                         @Query("s") int s
    );

    /**
     * ������������
     * @param User
     * @param admin
     * @param MsgID 88
     * @param Action
     * @param TotalTime
     * @param IntervalTime
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> setSnapShotConfig(@Query("User") String User,
                                           @Query("Psd") String admin,
                                           @Query("MsgID") int MsgID,
                                           @Query("Action") int Action,
                                           @Query("TotalTime") int TotalTime,
                                           @Query("IntervalTime") int IntervalTime,
                                           @Query("s") int s
    );


    /**
     * ��ȡssid����
     * @param User
     * @param admin
     * @param MsgID 36
     * @return
     */
    @GET("cfg1.cgi")
    Observable<List<SSIDModel>> getSSIDList(@Query("User") String User,
                                            @Query("Psd") String admin,
                                            @Query("MsgID") int MsgID,
                                            @Query("s") int s
    );




    //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=31&INFO_DevName=Hello,Ketty

    /**
     * �����豸����
     * @param User
     * @param admin
     * @param MsgID 31
     * @param INFO_DevName
     * @return
     */
    @GET("cfg1.cgi")
    Observable<RetModel> setDevName( @Query("User") String User,
                                     @Query("Psd") String admin,
                                     @Query("MsgID") int MsgID,
                                     @Query("INFO_DevName") String INFO_DevName,
                                     @Query("s") int s
    );

    /**
     * ��ȡwifi����
     * @param User
     * @param admin
     * @param MsgID 37
     * @return
     */
    @GET("cfg1.cgi")
    Observable<WIFIStateModel> getWifiState(@Query("User") String User,
                                            @Query("Psd") String admin,
                                            @Query("MsgID") int MsgID,
                                            @Query("s") int s
    );

    /**
     *
     * ȡ�ñ����������� WIFI ·�����б�
     * @param User
     * @param admin
     * @param MsgID 70
     * @return
     */
    @GET("cfg1.cgi")
    Observable<List<SSIDSavedModel>> getSavedSSIDInf(@Query("User") String User,
                                                     @Query("Psd") String admin,
                                                     @Query("MsgID") int MsgID,
                                                     @Query("s") int s
    );
}

package xyz.ezsky.ybutools.data.forums


/**
 * APP版本对象 ybu_app
 *
 * @author ruoyi
 * @date 2023-09-16
 */
class YbuApp {
    var id: Long? = null

    var url: String? = null

    var version: String? = null

    /** 是否强制  */
    var isMandatory: String? = null

    /** 版本描述  */
    var described: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
package xyz.ezsky.ybutools.data.jsxt.entity

data class ArtListData(
    val datas: datas,
    val code:String
)
data class datas(
    val cxlmxdggxx: Cxlmxdggxx,

)
data class Cxlmxdggxx(
    val totalSize: String,
    val pageNumber: String,
    val pageSize: String,
    val rows: List<Row>
)

data class Row(
    val GGBT: String?,
    val LMDM: String?,
    val SFZD: String?,
    val LLQX: String?,
    val FBBM_DISPLAY: String?,
    val FBR: String?,
    val FBBM: String?,
    val YDURL: String?,
    val LMMC: String?,
    val ZDZT: String?,
    val YXSJ: String?,
    val EJLM: String?,
    val GGZT: String?,
    val FBSJ: String?,
    val SFZD_DISPLAY: String?,
    val EJLM_DISPLAY: String?,
    val NRLX: String?,
    val FJ: String?,
    val FBRXM: String?,
    val PX: String?,
    val PCURL: String?,
    val GGDM: String?
)
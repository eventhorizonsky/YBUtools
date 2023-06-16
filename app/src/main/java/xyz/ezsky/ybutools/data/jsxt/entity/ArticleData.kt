package xyz.ezsky.ybutools.data.jsxt.entity

data class ArticleResponse(
    val code: String,
    val msg: String,
    val data: Data
)

data class Data(
    val GG_DATA: GGData,
    val PN_DATA: PNData,
    val TJ_DATA: List<Any>
)

data class GGData(
    val FBBMJC: String,
    val GGBT: String,
    val LMDM: String,
    val FBBM_DISPLAY: String,
    val LLQX: String,
    val FBBM: String,
    val FBR: String,
    val GGNR: String,
    val LHFBBM_DISPLAY: String,
    val FBSJ: String,
    val GGZT: String,
    val FBBMJC_DISPLAY: String,
    val TXFS_DISPLAY: String,
    val YDS: String,
    val GGDM: String
)

data class PNData(
    val NEXTGGDM: String,
    val NEXTGGBT_SUB: String,
    val NEXTGGBT: String,
    val PREGGDM: String,
    val PREGGBT: String,
    val PREGGBT_SUB: String
)


package xyz.ezsky.ybutools.data.jsxt.entity

data class ForumsPost(
    val createBy: String?,
    val createTime: String?,
    val updateBy: String?,
    val updateTime: String?,
    val remark: String?,
    val id: Int,
    val title: String,
    val description: String,
    val img: String,
    val view: String,
    val collected: String,
    val likes: String,
    val userId: Int,
    val userName: String,
    val userProfile: String,
    val postTime: String,
    val categoryId: Int
)
data class forumlist(
    val total:String,
    val rows:List<ForumsPost>,
    val code:String,
    val msg:String
)
data class forumresult(
    val msg:String,
    val code:String,
    val data:ForumsPost
)
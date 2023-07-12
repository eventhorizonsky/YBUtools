
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 为LazyColum增加加载更多提示
 * @param modifier Modifier
 * @param loadMoreState LoadMoreState
 * @param loadMore Function0<Unit>
 * @param content [@kotlin.ExtensionFunctionType] Function1<LazyListScope, Unit>
 */
@Composable
fun LazyLoadMoreColum(
    modifier: Modifier = Modifier,
    loadMoreState: LoadMoreState = LoadMoreState.hasMore,
    loadMoreCallBack: () -> Unit,
    content: LazyListScope.() -> Unit
) {
    val lazyListState = rememberLazyListState()
    lazyListState.OnBottomReached {
        if (loadMoreState == LoadMoreState.hasMore) {
            loadMoreCallBack()
        }
    }
    LazyColumn(modifier, state = lazyListState) {
        content()
        //LoadMore的提示内容,可以根据自己的需求去自定义该部分显示样式
        item {
            Box(
                modifier = Modifier
                    .clickable {
                        if (loadMoreState == LoadMoreState.loadError) {
                            loadMoreCallBack()
                        }
                    }
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (loadMoreState) {
                        LoadMoreState.hasMore -> "正在加载.."
                        LoadMoreState.noMore -> "没有更多数据了.."
                        LoadMoreState.loadError -> "网络出错，点击重试！"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
enum class LoadMoreState {
    hasMore,    //可加载更多内容
    noMore,     //已加载完全部内容
    loadError   //加载出错
}

/**
 * 扩展LazyColum的LazyListState，通过计算判断是否到达最后一项
 * @receiver LazyListState
 * @param buffer Int 指定离底部还剩几个时进行加载更多回调
 * @param loadMore Function0<Unit>
 */
@Composable
fun LazyListState.OnBottomReached(buffer: Int = 0, loadMore: () -> Unit) {
    require(buffer >= 0) {
        "buffer 值必须是正整数"
    }
    //是否应该加载更多的状态
    val shouldLoadMore = remember {
        //因为状态由layoutInfo计算得到
        derivedStateOf {
            //列表为空的话直接返回true
            val lastVisibleItem =
                layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
            //如果现实项为最后一个item 返回true
            lastVisibleItem.index == layoutInfo.totalItemsCount - 1 - buffer
        }
    }
    LaunchedEffect(key1 = shouldLoadMore, block = {
        snapshotFlow {
            shouldLoadMore.value
        }.collect {
            if (it) {
                loadMore()
            }
        }
    })
}
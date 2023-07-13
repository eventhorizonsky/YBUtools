package xyz.ezsky.ybutools.ui.toolcompose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import xyz.ezsky.ybutools.data.jsxt.entity.Grades

/**
 * 错误弹窗
 */
@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "发生错误",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontSize = 16.sp
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "OK")
            }
        }
    )
}
@Composable
fun gradeDialog(
    grade: Grades,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = grade.courseName,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Column {
                Text(text = "课程学期：" + grade.kkxq)
                Text(text = "课程编号：" + grade.kcbh)
                Text(text = "课程名称：" + grade.courseName)
                Text(text = "成绩：" + grade.score)
                Text(text = "特殊原因：" + grade.tsyy)
                Text(text = "学分：" + grade.xf)
                Text(text = "总学时：" + grade.zxs)
                Text(text = "绩点：" + grade.jd)
                Text(text = "补重学期：" + grade.bcxq)
                Text(text = "考核方式：" + grade.khfs)
                Text(text = "考试性质：" + grade.ksxz)
                Text(text = "课程属性：" + grade.kcsx)
                Text(text = "课程性质：" + grade.kcxz)
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(text = "OK")
            }
        }
    )
}

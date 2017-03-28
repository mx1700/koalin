import me.mx1700.koalin.jetty.listen
import me.mx1700.koalin.router.Router
import me.mx1700.koalin.servlet.Application

fun main(args: Array<String>) {
    val app = Application()
    app.use(Router()) {

        get("/") {
            "hello world"
        }

        get("/user") {
            response.headers["test-header"] = "1.1"
            next()
        }

        get("/user[/{name}]") {
            "hello ${params["name"]}"
        }
    }
    app.listen(9000)
}
package org.scalatra

import test.scalatest.ScalatraFunSuite

class ErrorHandlerTest extends ScalatraFunSuite {
  trait TestException extends RuntimeException
  case class Exception1() extends TestException
  case class Exception2() extends TestException

  class BaseServlet extends ScalatraServlet {
    get("/1") {
      status(418)
      throw new Exception1
    }
    get("/uncaught") { throw new RuntimeException }
    error { case e: TestException => "base" }
  }

  class ChildServlet extends BaseServlet {
    get("/2") { throw new Exception2 }
    error { case e: Exception2 => "child" }
  }

  addServlet(new BaseServlet, "/base/*")
  addServlet(new ChildServlet, "/child/*")

  test("result of error handler should be rendered") {
    get("/base/1") {
      body should equal ("base")
    }
  }

  test("error handlers are composable") {
    get("/child/2") {
      body should equal ("child")
    }

    get("/child/1") {
      body should equal ("base")
    }
  }

  test("response status should not be set on error") {
    get("/base/1") {
      status should equal (418)
    }
  }

  test("rethrows uncaught exceptions") {
    get("/base/uncaught") {
      status should equal (500)
    }
  }
}

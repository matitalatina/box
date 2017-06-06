//package ch.wsl.box.client.views
//
//import ch.wsl.box.client.ModelsState
//import io.udash._
//
///**
//  * Created by andre on 6/6/2017.
//  */
//object CustomModelsViewPresenter extends ViewPresenter[ModelsState] {
//  import scalajs.concurrent.JSExecutionContext.Implicits.queue
//
//
//  override def create(): (View, Presenter[ModelsState]) = {
//    val model = ModelProperty{
//      Models(Seq(),None,None,"",Seq())
//    }
//    val presenter = new ModelsPresenter(model)
//    val view = new ModelsView(model,presenter,None,None,false)
//    (view,presenter)
//  }
//}
//

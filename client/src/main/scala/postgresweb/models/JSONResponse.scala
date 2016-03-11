package postgresweb.models

import io.circe.Json

/**
  * Created by andreaminetti on 23/02/16.
  */
case class JSONResponse(count:Int, data:Vector[Map[String,Json]])
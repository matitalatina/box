package ch.wsl.box.client

import ch.wsl.box.model.shared.{JSONSchema}
import org.scalatest.flatspec.AnyFlatSpec

object JsonTest extends AnyFlatSpec {

  "An empty Set" should "have size 0" in {
    assert(Set.empty.size == 0)
  }

  it should "produce NoSuchElementException when head is invoked" in {
    assertThrows[NoSuchElementException] {
      Set.empty.head
    }
  }
}

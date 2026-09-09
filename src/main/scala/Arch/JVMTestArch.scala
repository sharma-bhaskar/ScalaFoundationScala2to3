package Arch

object TestArch {

  /**
   * abc.scala code -- compile throught scalac  than its create abc.class than it load to class loader to verify the byte codes after that it assignement happen which
   * data will go to where that we call it runtime data
   * Scala source code
   * ↓
   * scalac compiler
   * ↓
   * .class bytecode files
   * ↓
   * JVM classloader
   * ↓
   * bytecode verifier
   * ↓
   * interpreter + JIT compiler
   * ↓
   * machine code
   *
   * Than it load it JVM Process Prentend to virtual cpu and it interpret each instructions
   *
   * class loader helps to load -- verify -- prepare -- resolve -- initialize
   * Class Loader -- Bootstrap Loader( load core Jdk)   ----------- Platform Loader (jdk modules)  ----------- Application Loader (classpath )
   * ↓
   * RunTime Data Areas --- it has three areas Heap  ------- Stack ------ Metaspaces
   *
   * Heap and metaspace shared across all the thread but stack/pc/register is per thread
   *
   * ↓
   * Execution Engine --  this will help to execute the code in this interpreter(bytes codes execution), jit(c1 and c2) and GC
   *
   *
   *
   *
   *
   * */


  case class User(name: String, age: Int)

  def process(user: List[User]): List[String] = {
    user.map(u => s"${u.name} - ${u.age}")
  }

  def main(args: Array[String]): Unit = {
    val users = List(
      User("A", 10),
      User("B", 20),
      User("C", 30)
    )
    val result = process(users)
    println(result)

  }

  /**
   * this code first compile and  run and convert it to .class file and JVM load .class file and run it
   *
   * so compiler uses scalac
   *
   * So JVM load
   *
   * MetaSpace in this space it load classes User scala library class and all  -  Memory -- interperter -- jit
   *
   * The execution is
   * Main -- thread
   * --- user -- (reference)
   * --- result -- (reference)
   *
   * So stack store references not actual objects and each method call is new stack frame
   *
   *
   * and Now Heap storage all the objects creation
   * like user -- will go to heap but his ref will store in stack
   * Heap:
   * ├── User("A",10)
   * ├── User("B",20)
   * ├── User("C",30)
   * ├── List node → User A
   * ├── List node → User B
   * ├── List node → User C
   *
   * now the function calls so it will create the method stack val result = process(user)
   * Stack
   * main--
   * user
   * result
   * process()
   * user -- reference
   *
   *
   * and now the process map operation is more heap stack
   *
   * so Heap look like this
   *
   * Heap
   * Function objects (closure)
   * String-- A -- 10
   * String B -- 20
   * String C -- 30
   * -- New List nodes (result list)
   *
   * Lambda store more memory -- If lambda captures large data → memory stays longer
   * 6. After process returns
   * Stack:
   * └── main()
   * ├── users
   * ├── result
   *   means if method returns it will remove from the stacks thread
   *
   *   7.  Garbage Collection (GC)
   *    Now JVM checks:
   *
   *    What is still ref?
   *    users --- refe -- stays
   *    result -- refer -- stays
   *    If something is not referenced
   *    GC removes it from heap
   *
   *
   *
   *
   * */


}

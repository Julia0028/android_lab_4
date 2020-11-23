# Лабораторная работа №4. RecyclerView.

### Цели

- Ознакомиться с принципами работы adapter-based views.
- Получить практические навки разработки адаптеров для view

### Задачи


#### Задача 1. Знакомсотво с библиотекой (unit test)
---
**Задание**: Ознакомиться со strict mode библиотеки, проиллюстрировав его работу unit-тестом.

**Решение**: Была рассмотена библиотека, предоставляющая доступ к записям  формате bibtex. Она имеет 2 режима работы: normal и strict. В strict mode работает искусственное ограничение: в памяти нельзя хранить более maxValid=20 экземляров BibEntry одновременно, поэтому в strictModeThrowsException() при запросе записи в 21 раз и при попытке получить тип 1ой записи (getType()) ловится IOE exception.

**strictModeThrowsException()**
```
 @Test
  public void strictModeThrowsException() throws IOException {
    BibConfig cfg = database.getCfg();
    cfg.strict = true;
    BibEntry first = database.getEntry(0);
    for (int i = 0; i < cfg.maxValid - 1; i++) {
      BibEntry unused = database.getEntry(0);
      Assert.assertNotNull("Should not throw any exception @" + i, first.getType());
    }
    try {
      BibEntry twentyFirst = database.getEntry(0);
      first.getType();
    } catch (IllegalStateException e) {
      System.out.println("Throw IllegalStateException with message: " + e.getMessage());
    }
  }
```

Для проверки shuffle был создан новый .bibtex файл с записями из следующего задания. В методе shuffleFlag() сначала создается экземпляр database1. Далее в цикле создаются новые экземплляры database2 с включенным shuffle, все элементы которых сравниваются с элементами database1. Если порядки записей совпадут, noDifference станет true и тест упадет.
Есть вероятность, что "нашафлится" порядок записей, равный начальному, но она очень маленькая.

**shuffleFlag()**
```
 @Test
  public void shuffleFlag() throws IOException {
    BibConfig cfg = new BibConfig();
    int random = 100;
    boolean noDifferences = false;
    cfg.shuffle = false;
    BibDatabase database1 = shuffleDatabase("/shuffle.bib", cfg);
    for (int i = 0; i < random; i++) {
      cfg.shuffle = true;
      BibDatabase database2 = shuffleDatabase("/shuffle.bib", cfg);
      for (int j = 0; j < database1.count(); j++)
      if (database1.getEntry(j).equals(database2.getEntry(j))) noDifferences = true;
    }
    Assert.assertFalse(noDifferences);
  }
```


#### Задача 2. Знакомство с RecyclerView.
---
**Задание**: написать Android приложение, которое выводит все записи из bibtex файла на экран, используя предложенную библиотеку и RecyclerView. 
Однородный список: в качестве исходных данных используется файл articles.bib.

**Решение**: 

Подключим библиотеку biblib:
**settings.gradle**: 
```
include ':biblib'
include ':app'
rootProject.name = "task_2"
```
**build.gradle**: 
```
dependencies {
    implementation project (':biblib')
```

Чтобы реализовать RecyclerView, мы должны создать следующие компоненты:
- RecyclerView, который мы должны добавить в layout нашего экрана;
- layout для каждой строки списка;
- адаптер, который содержит данные и связывает их со списком.

Добавление RecyclerView в layout activity_main.xlm:
```
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:layout_editor_absoluteX="256dp"
        tools:layout_editor_absoluteY="237dp" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Создание layout для строки списка:
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="72dp"
        android:layout_height="55dp"
        android:layout_gravity="center"
        app:srcCompat="@android:drawable/btn_star_big_on"
        tools:layout_editor_absoluteX="40dp"
        tools:layout_editor_absoluteY="40dp"
        android:layout_marginStart="10dp"
        android:layout_marginLeft="10dp" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <TextView
            android:layout_marginTop="20dp"
            android:layout_marginBottom="10dp"
            android:id="@+id/title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:textSize="18sp"
            android:text="title"
            android:layout_marginLeft="10dp"
            android:layout_marginRight="20dp" />

        <TextView
            android:id="@+id/author"
            android:layout_marginBottom="20dp"
            android:layout_marginRight="20dp"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="author"
            android:textSize="14sp"
            android:layout_weight="1"
            android:layout_marginLeft="10dp"
            />
    </LinearLayout>

</LinearLayout>
```

Создание RecyclerView в Activity:
В созданном экземпляре RecyclerView мы должны установить LayoutManager. LayoutManager отображает список в определённой форме. В данном случае используется LinearLayoutManager, который показывает данные в простом списке – вертикальном или горизонтальном (по умолчанию вертикальном). 
```
package com.example.task_2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val articles: InputStream = resources.openRawResource(R.raw.task_2)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BiblibAdapter(articles)

    }
}
```

Создание класса BiblibAdapter:
 Класс обеспечивает привязку данных библиотеки Biblib к View (в данном случае TextView), которые отображает RecyclerView. В конструкторе класса InputStreamReader() читает файл .bib, создаётся экземпляр класса BibDatabase(). BibLibAdapter содержит в себе класс ViewHolder, который отвечает за отображение данных в TextView. Также необходимо переопределить некоторые методы:

- getItemCount() возвращает общее количество элементов списка
- onCreateViewHolder() - создание объекта ViewHolder. layout строки списка передается объекту ViewHolder.
- onBindViewHolder() принимает объект ViewHolder и устанавливает необходимые данные для соответствующей строки во view-компоненте.

```
package com.example.task_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import name.ank.lab4.BibDatabase
import name.ank.lab4.BibEntry
import name.ank.lab4.Keys
import java.io.InputStream
import java.io.InputStreamReader

class BiblibAdapter(inputStream: InputStream): RecyclerView.Adapter<BiblibAdapter.ViewHolder>() {
    private lateinit var database: BibDatabase

    init {
        InputStreamReader(inputStream).use { reader ->
            database = BibDatabase(reader)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val author: TextView


        init {
            // Define click listener for the ViewHolder's View.
            title = view.findViewById(R.id.title)
            author = view.findViewById(R.id.author)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return database.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry: BibEntry = database.getEntry(position)
        holder.title.text = entry.getField(Keys.TITLE)
        holder.author.text = entry.getField(Keys.AUTHOR)
    }


}
```

Получилось такое риложение:
![image_task2](https://github.com/Julia0028/android_lab_4/blob/master/pictures/2.png)



#### Задача 3. Бесконечный список.
---
**Задание**: сделать список из предыдущей задачи бесконечным: после последнего элемента все записи повторяются, начиная с первой.

**Решение**: 
В методе onBindViewHolder() будем брать запись по остатку от деления всего количества записей.
В методе getItemCount() возвращаем Integer.MAX_VALUE
```
   override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry: BibEntry = database.getEntry(position % database.count())
        holder.title.text = entry.getField(Keys.TITLE)
        holder.author.text = entry.getField(Keys.AUTHOR)
    }
}
```


### Выводы
В данной работе была исследована библиоткеа biblib , с помощью тестов проверена работа флагов strict и shuffle.
Также было осуществлено знакомство с RecyclerView. RecyclerView позволяет эффективно отображать большие наборы данных. Когда элемент прокручивается с экрана, RecyclerView не уничтожает его view. Вместо этого происходит повторное использование view для новых элементов, которые прокручиваются на экране. Такое повторное использование значительно повышает производительность приложения.
Результат работы - приложение, которое отображает записи .bib файла с помощью  рассмотренной библиотеки и RecyclerView. 

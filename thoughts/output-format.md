# Output format
![](img/simple-tree.png)
```
s start
s fun1
s fun3
s fun4
f fun4
s fun4
f fun4
s fun5
s fun6
f fun6
f fun5
f fun3
f fun1
s fun2
f fun2
f start
```


Нужно разделять функции с разных потоков
Варинты:
* писать данные с разных потоков в разные файлы
* писать в один файл, но в разные секции (как тогда следить, где в данный момент находится секция?)
* писать всё в одну кучу, в начале каждой строчки ставить хеш код (или id) потока

Два потока выполнили одинаковую последовательность методов:
```
0 s start
1 s start
1 s fun1
0 s fun1
1 s fun3
0 s fun3
0 s fun4
1 s fun4
1 f fun4
1 s fun4
0 f fun4
0 s fun4
0 f fun4
0 s fun5
1 f fun4
1 s fun5
...
```

Получается, профайлер будет только регистрировать начало и конец метода.

Если мы не хотим дублировать имена методов и длинные номера потоков, то нужно еще хранить:
* id потока
* имя потока
* Map methodName -> methodId

Для этого нужно либо записывать эту информацию в начало файла, либо прямо в поток.

```
def_thread main 0
def_fun start 0
0 s 0
def_thread someTread 1
1 s 0
def_fun fun1 1
1 s 1
0 s 1
def_fun fun3 2
1 s 2
0 s 2
def_fun fun4 3
0 s 3
1 s 3
1 f 3
1 s 3
0 f 3
0 s 3
0 f 3
def_fun fun5 4
0 s 4
1 f 3
1 s 4
...
```




# Protocol

## 공통 
+ **Databits** 8
+ **Baudrate** 19200
+ **Stop Bits** 1
+ **Parity** N

## 싸인보드
| 0(Start) | 1(Device Type) | 2(Power)  |  3-2bit  |  3-2bit    |  3-4bit    |  4(순차 동시) | 
|   :---:  |     :---:      | :---:     |   :---:  |   :---:    |    :---:    |  :---:      |
|   0xaa   |     0x01       | 0x01(켜짐) | 0x01(빠름)| 0x01(주간)   |  0x01(좌측)  | 0x01(동시)
|          |                | 0x00(꺼짐) | 0x02(중간)| 0x02(흐린날) |  0x02(양방향) | 0x02(순차)
|          |                |           | 0x03(느림)| 0x03(야간)   |  0x04(우측)  |
|          |                |           |          |            |   0x08(엑스)  |

## 전광판 
 1 . 단축번호 전송 
|  0(Start) | 1(Device Type) | 2(Power)  |   3(Img Number)  | 
|    ---    |         :---:  |   :---:   |         :---:    |
|    0xaa   |         0x03   |  0x01(켜짐)|  0x01 ~ 0x1b(1~27) | 
|           |                | 0x02(꺼짐) |                    |
|           |                |           |                     |

2. 밝기 전송 

|  0(Start) | 1(Device Type) | 2(Power)   |  3(밝기) | 
|    ---    |      :---:     |   :---:    |  :---:  |
|    0xaa   |      0x03      |  0x01(켜짐) |   (1~5) | 
|           |                |  0x02(꺼짐) |         |
|           |                |            |         |




## 싸이렌 
|  0(Start) | 1(Device Type) | 2(Power)  |   3(Data)  | 4(볼륨) | 
|    ---    |         :---:  |   :---:   |   :---:    | :---:  |
|    0xaa   |         0x03   |  0x01(켜짐)|  0x01(경찰)  |   1    |
|           |                | 0x02(꺼짐) |  0x02(소방)  |   2    |
|           |                |           |  0x04(구급)  |   3    |
|           |                |           |  0x06(음성1) |   4    |
|           |                |           |  0x07(음성2) |   5    |
|           |                |           |  0x08(음성3) |        |
|           |                |           |  0x09(음성4) |        |
|           |                |           |  0x0a(음성5) |        |


## 비상등 
|  0(Start) | 1(Device Type) | 2(Power)  |  
|    ---    |         :---:  |   :---:   |   
|    0xaa   |         0x04   |  0x01(켜짐)|  
|           |                | 0x02(꺼짐) |  
|           |                |           |  
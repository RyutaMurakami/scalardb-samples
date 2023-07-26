import tkinter as tk
from tkinter import *
import subprocess
import time
import json
import re

initial_cmd = ["docker-compose up -d",
    "java -jar scalardb-schema-loader-3.9.1.jar --config database.properties --schema-file schema.json --coordinator",
    "./gradlew run --args=\"LoadInitialData\""]

def search_customer():
    input_text = entry_1.get()
    cmd = f"./gradlew run --args=\"GetCustomerInfo {input_text}\""
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout
    print(res)

    start = '{'
    end = '}'
    subres = res[res.index(start)+1:res.index(end)].strip()
    data = re.split(": |, ", subres)

    label_1b.config(text=f"ID: {data[1]}\nName: {data[3][1:-1]}\nCredit Number: {data[5]}\nAddress: {data[7]}")

def search_item():
    input_text = entry_2.get()
    cmd = f"./gradlew run --args=\"GetItemInfo {input_text}\""
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout
    print(res)

    start = '['
    end = ']'
    subres = res[res.index(start)+1:res.index(end)].strip()
    items = re.split("}, {|{|}", subres)
    items.remove('')
    data = []
    for item in items:
        data.append(re.split(": |, |,", item))
    data.remove([''])
    
    txt = ""
    for i in range(len(data)):
        txt += f"Name: {data[i][1][1:-1]}\nPrice: {data[i][3]}\nShop ID: {data[i][5]}\nStock: {data[i][7]}\n\n"
    label_2b.config(text=txt)

def exe_item_increase():
    input_text1 = entry_3a.get()
    input_text2 = entry_3b.get()
    cmd = f"./gradlew run --args=\"IncreaseItemStock {input_text1} {input_text2}\""
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    print(res.stdout)
    res = res.strerr

    if "at" in res:
        label_3b.config(text="Error")
    else:
        label_3b.config(text="Success")

def exe_item_add():
    input_text1 = entry_3c.get()
    input_text2 = entry_3d.get()
    input_text3 = entry_3e.get()
    input_text4 = entry_3f.get()
    input_text5 = entry_3g.get()
    cmd = f"./gradlew run --args=\"AddItemCommand {input_text1} {input_text2} {input_text3} {input_text4} {input_text5}\""
    res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    print(res.stdout)

    if "at" in res.stderr or "parameters" in res.stderr:
        label_3d.config(text="Error")
    elif "already" in res.stdout:
        label_3d.config(text="The item already exists.")
    else:
        label_3d.config(text="Success")


def main():
    subprocess.run(initial_cmd[0], shell=True)
    time.sleep(20)
    subprocess.run(initial_cmd[1], shell=True)
    subprocess.run(initial_cmd[2], shell=True)

    root = tk.Tk()
    root.geometry("1080x480")
    root.resizable(False, False)
    root.title("EC Cite")

    frame1 = Frame(root, width=360, height=480, borderwidth=2, relief='solid')
    frame2 = Frame(root, width=360, height=480, borderwidth=2, relief='solid')
    frame3 = Frame(root, width=360, height=480, borderwidth=2, relief='solid')
    frame1.propagate(False) 
    frame2.propagate(False)
    frame3.propagate(False)
    frame1.grid(row=0, column=0)
    frame2.grid(row=0, column=1)
    frame3.grid(row=0, column=2)

    # frame1
    label_1a = Label(frame1, text='Please enter customer ID', font=('System', 20))
    global entry_1
    entry_1 = Entry(frame1)
    button_1 = Button(frame1, text='search', command=search_customer, font=('System', 20))
    global label_1b
    label_1b = Label(frame1, text='', font=('System', 20))
    label_1a.pack()
    entry_1.pack()
    button_1.pack()
    label_1b.pack()

    # frame2
    label_2a = Label(frame2, text='Please enter item name', font=('System', 20))
    global entry_2
    entry_2 = Entry(frame2)
    button_2 = Button(frame2, text='search', command=search_item, font=('System', 20))
    global label_2b
    label_2b = Label(frame2, text='', font=('System', 20))
    label_2a.pack()
    entry_2.pack()
    button_2.pack()
    label_2b.pack()

    # frame3
    label_3a = Label(frame3, text='Please enter item ID and its increase', font=('System', 20))
    global entry_3a
    entry_3a = Entry(frame3)
    global entry_3b
    entry_3b = Entry(frame3)
    button_3a = Button(frame3, text='decision', command=exe_item_increase, font=('System', 20))
    global label_3b
    label_3b = Label(frame3, text='', font=('System', 20))
    label_3a.pack()
    entry_3a.pack()
    entry_3b.pack()
    button_3a.pack()
    label_3b.pack()

    label_3c = Label(frame3, text='Enter the item you want to add\n(ID, Name, Price, ShopID, Stock)', font=('System', 20))
    global entry_3c
    entry_3c = Entry(frame3)
    global entry_3d
    entry_3d = Entry(frame3)
    global entry_3e
    entry_3e = Entry(frame3)
    global entry_3f
    entry_3f = Entry(frame3)
    global entry_3g
    entry_3g = Entry(frame3)
    button_3b = Button(frame3, text='decision', command=exe_item_add, font=('System', 20))
    global label_3d
    label_3d = Label(frame3, text='', font=('System', 20))
    label_3c.pack()
    entry_3c.pack()
    entry_3d.pack()
    entry_3e.pack()
    entry_3f.pack()
    entry_3g.pack()
    button_3b.pack()
    label_3d.pack()

    root.mainloop()

if __name__ == "__main__":
    main()

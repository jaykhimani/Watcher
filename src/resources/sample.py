import sys

print("hello")
argss = str(sys.argv)
f = open('/tmp/frm.txt', 'w')
f.write('Hello There -- \n')
f.write(argss)
f.close()


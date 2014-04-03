from __future__ import print_function

import os
import cgi
from subprocess import Popen, PIPE, STDOUT

SCRIPTDIR = 'javaprolog'
SCRIPT = ['java', '-cp', '.;json-simple-1.1.1.jar;gnuprologjava-0.2.6.jar;./main/.', 'main.Shrdlite']

while not os.path.isdir(SCRIPTDIR):
    SCRIPTDIR = os.path.join("..", SCRIPTDIR)

print('Content-type:text/plain')
print()

try:
    form = cgi.FieldStorage()
    data = form.getfirst('data')
    script = Popen(SCRIPT, cwd=SCRIPTDIR, stdin=PIPE, stdout=PIPE, stderr=PIPE)
    out, err = script.communicate(data)
    print(out)
    if err:
        raise Exception(err)

except:
    import sys, traceback
    print(traceback.format_exc())
    sys.exit(1)

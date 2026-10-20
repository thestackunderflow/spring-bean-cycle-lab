"""Checks the recorded logs (evidence/logs, written by run-scenarios.sh) say what the video says, and that every line
the video shows in a terminal panel was copied from a real log.
Run: python -m unittest discover -s tests -p "test_evidence.py" -v"""
import glob, json, os, re, unittest

HERE = os.path.dirname(os.path.abspath(__file__))
LOGS = os.path.join(HERE, '..', 'evidence', 'logs')
# The video's script (its "primer") is not part of this repo. Point SBC_PRIMER at a copy to run the panel check;
# without it that one test is skipped.
PRIMER = os.environ.get('SBC_PRIMER', '')
HEADLINE = 'The dependencies of some of the beans in the application context form a cycle:'


def log(name):
    with open(os.path.join(LOGS, name + '.log'), encoding='utf8') as f:
        return f.read()


def exit_code(name):
    return int(re.search(r'exit code: (\d+)\s*$', log(name)).group(1))


class Version41(unittest.TestCase):
    def test_banner_is_4_1_1(self):
        self.assertIn('(v4.1.1)', log('constructor-cycle'))

    def test_constructor_cycle_fails_and_no_constructor_ran(self):
        self.assertEqual(exit_code('constructor-cycle'), 1)
        self.assertIn(HEADLINE, log('constructor-cycle'))
        self.assertNotIn('LAB: creating', log('constructor-cycle'))

    def test_flag_does_not_fix_a_constructor_cycle(self):
        self.assertEqual(exit_code('constructor-cycle--allow'), 1)
        self.assertIn('Despite circular references being allowed, the dependency cycle between beans could not be broken.',
                      log('constructor-cycle--allow'))

    def test_field_and_setter_cycles_fail_by_default_but_both_objects_were_built(self):
        for s in ('field-cycle', 'setter-cycle'):
            self.assertEqual(exit_code(s), 1, s)
            self.assertIn('LAB: creating OrderService', log(s))
            self.assertIn('LAB: creating PaymentService', log(s))

    def test_field_and_setter_cycles_start_with_the_flag(self):
        for s in ('field-cycle--allow', 'setter-cycle--allow'):
            self.assertEqual(exit_code(s), 0, s)
            self.assertIn('LAB: started. order 42: PAID', log(s))

    def test_mixed_loop_depends_on_which_bean_spring_builds_first(self):
        # field bean (OrderService) built first: the flag rescues it
        self.assertEqual(exit_code('mixed-field-first'), 1)
        l = log('mixed-field-first--allow')
        self.assertEqual(exit_code('mixed-field-first--allow'), 0)
        self.assertLess(l.index('LAB: creating OrderService (field)'), l.index('LAB: creating PaymentService (constructor)'))
        self.assertIn('LAB: started. order 42: PAID', l)
        # constructor bean (OrderService) first: the flag can't help, and its constructor never ran
        l = log('mixed-constructor-first--allow')
        self.assertEqual(exit_code('mixed-constructor-first--allow'), 1)
        self.assertIn('Despite circular references being allowed', l)
        self.assertNotIn('LAB: creating OrderService', l)

    def test_every_fix_starts_and_pays(self):
        for s in ('fix-extract', 'fix-events', 'fix-lazy', 'fix-provider'):
            self.assertEqual(exit_code(s), 0, s)
            self.assertIn('LAB: started. order 42: PAID', log(s))

    def test_lazy_injects_a_proxy_and_still_builds_payment_at_startup(self):
        l = log('fix-lazy')
        self.assertIn('handed a PaymentService$$SpringCGLIB$$0', l)
        self.assertLess(l.index('LAB: creating PaymentService'), l.index('Started CycleLab'))


class OldVersions(unittest.TestCase):
    """The 2.6 change, shown with the last 2.5 and 2.6 releases."""

    def test_banners(self):
        self.assertIn('(v2.5.15)', log('boot-2.5-field-cycle'))
        self.assertIn('(v2.6.15)', log('boot-2.6-field-cycle'))

    def test_2_5_starts_a_field_cycle_2_6_refuses_it(self):
        self.assertEqual(exit_code('boot-2.5-field-cycle'), 0)
        self.assertIn('LAB: started. order 42: PAID', log('boot-2.5-field-cycle'))
        self.assertEqual(exit_code('boot-2.6-field-cycle'), 1)
        self.assertIn(HEADLINE, log('boot-2.6-field-cycle'))

    def test_2_5_already_refused_a_constructor_cycle(self):
        self.assertEqual(exit_code('boot-2.5-constructor-cycle'), 1)
        self.assertIn(HEADLINE, log('boot-2.5-constructor-cycle'))


class PanelsCopiedFromLogs(unittest.TestCase):
    """Every terminal-panel line in the video's primer is in a real log. '…' marks a cut: each piece must appear, in
    order, on one log line. Lines starting with '$ ' (a command) or '# ' (a note) are exempt."""

    @unittest.skipUnless(os.path.isfile(PRIMER), 'video primer not present (set SBC_PRIMER to check it)')
    def test_terminal_lines_are_real(self):
        logs = []
        for f in glob.glob(os.path.join(LOGS, '*.log')):
            with open(f, encoding='utf8') as fh:
                logs += fh.read().splitlines()
        with open(PRIMER, encoding='utf8') as fh:
            primer = json.load(fh)
        checked = 0
        for c in primer['chapters']:
            for b in c['beats']:
                p = b.get('panel')
                if not p or p.get('kind') != 'terminal':
                    continue
                for line in p['lines']:
                    if line.startswith('$ ') or line.startswith('# '):
                        continue
                    pieces = [x for x in line.split('…') if x]
                    ok = any(self._in_order(pieces, l) for l in logs)
                    self.assertTrue(ok, f'{b["id"]}: not in any log: {line!r}')
                    checked += 1
        self.assertGreater(checked, 10)

    @staticmethod
    def _in_order(pieces, line):
        i = 0
        for p in pieces:
            j = line.find(p, i)
            if j < 0:
                return False
            i = j + len(p)
        return True


if __name__ == '__main__':
    unittest.main()

/* =========================================
   LIBRARYHUB · LIBRARY MANAGEMENT SYSTEM
   Interactions & animations
   ========================================= */

(function () {
    'use strict';

    var $ = function (s, c) { return (c || document).querySelector(s); };
    var $$ = function (s, c) { return Array.prototype.slice.call((c || document).querySelectorAll(s)); };
    var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var finePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches;

    /* ---------- 1. Split hero title into animated characters ---------- */
    function splitTitle() {
        var title = $('#heroTitle');
        if (!title) return;
        var index = 0;

        function walk(node) {
            Array.prototype.slice.call(node.childNodes).forEach(function (child) {
                if (child.nodeType === 3) {
                    var frag = document.createDocumentFragment();
                    child.textContent.split(/(\s+)/).forEach(function (part) {
                        if (!part) return;
                        if (/^\s+$/.test(part)) {
                            frag.appendChild(document.createTextNode(' '));
                            return;
                        }
                        var word = document.createElement('span');
                        word.className = 'word';
                        part.split('').forEach(function (ch) {
                            var c = document.createElement('span');
                            c.className = 'char';
                            c.style.setProperty('--i', index++);
                            c.textContent = ch;
                            word.appendChild(c);
                        });
                        frag.appendChild(word);
                    });
                    child.parentNode.replaceChild(frag, child);
                } else if (child.nodeType === 1) {
                    walk(child);
                }
            });
        }
        walk(title);
        title.setAttribute('aria-label', title.textContent.replace(/\s+/g, ' ').trim());
    }

    /* ---------- 2. Build the 3D bookshelf ---------- */
    function buildShelves() {
        var colors = ['#3b6cff', '#e2b25c', '#0f766e', '#9f1d2e', '#6d3a9c', '#475569', '#1d8fd6', '#c2570c', '#166534'];
        var rows = [
            { el: $('#shelfTop'), titles: ['Atlas', 'Physics', 'Poetry', 'History', 'Design', 'Fiction', 'Botany', 'Maths', 'Music', 'Law'] },
            { el: $('#shelfBottom'), titles: ['Art', 'Novels', 'Science', 'Memoir', 'Logic', 'Travel', 'Code', 'Myths', 'Space'] }
        ];

        var n = 0;
        rows.forEach(function (row) {
            if (!row.el) return;
            row.titles.forEach(function (t, k) {
                var b = document.createElement('div');
                b.className = 'spine';
                b.style.setProperty('--h', (120 + Math.round(Math.random() * 62)) + 'px');
                b.style.setProperty('--w', (28 + Math.round(Math.random() * 14)) + 'px');
                b.style.setProperty('--c', colors[(n * 4 + k * 3) % colors.length]);
                b.style.setProperty('--i', n++);
                b.innerHTML = '<em>' + t + '</em>';
                row.el.appendChild(b);
            });
            // one leaning book at the end of every row
            var lean = document.createElement('div');
            lean.className = 'spine lean';
            lean.style.setProperty('--h', '150px');
            lean.style.setProperty('--w', '30px');
            lean.style.setProperty('--r', '14deg');
            lean.style.setProperty('--c', '#e2b25c');
            lean.style.setProperty('--i', n++);
            lean.innerHTML = '<em>Next</em>';
            row.el.appendChild(lean);
        });
    }

    /* ---------- 3. Mouse parallax on the shelf ---------- */
    function shelfParallax() {
        var scene = $('#scene');
        var wrap = $('#shelfWrap');
        if (!scene || !wrap || reduceMotion || !finePointer) return;

        var hero = $('#home');
        wrap.style.transform = 'rotateY(-14deg) rotateX(6deg)';
        hero.addEventListener('mousemove', function (e) {
            var r = hero.getBoundingClientRect();
            var x = (e.clientX - r.left) / r.width - 0.5;
            var y = (e.clientY - r.top) / r.height - 0.5;
            wrap.style.transform = 'rotateY(' + (-14 + x * 16).toFixed(2) + 'deg) rotateX(' + (6 - y * 10).toFixed(2) + 'deg)';
        });
        hero.addEventListener('mouseleave', function () {
            wrap.style.transform = 'rotateY(-14deg) rotateX(6deg)';
        });
    }

    /* ---------- 4. Particle field (floating dust of pages) ---------- */
    function particles() {
        var canvas = $('#particles');
        if (!canvas || reduceMotion) return;
        var ctx = canvas.getContext('2d');
        var w, h, dots = [], raf, running = true;
        var mouse = { x: -999, y: -999 };

        function resize() {
            var dpr = Math.min(window.devicePixelRatio || 1, 2);
            w = canvas.clientWidth;
            h = canvas.clientHeight;
            canvas.width = w * dpr;
            canvas.height = h * dpr;
            ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
            var count = Math.min(70, Math.floor((w * h) / 18000));
            dots = [];
            for (var i = 0; i < count; i++) {
                dots.push({
                    x: Math.random() * w,
                    y: Math.random() * h,
                    r: Math.random() * 1.8 + 0.5,
                    vx: (Math.random() - 0.5) * 0.25,
                    vy: -Math.random() * 0.35 - 0.05,
                    a: Math.random() * 0.5 + 0.2
                });
            }
        }

        function frame() {
            if (!running) return;
            ctx.clearRect(0, 0, w, h);
            for (var i = 0; i < dots.length; i++) {
                var d = dots[i];
                d.x += d.vx;
                d.y += d.vy;
                if (d.y < -10) { d.y = h + 10; d.x = Math.random() * w; }
                if (d.x < -10) d.x = w + 10;
                if (d.x > w + 10) d.x = -10;

                // gentle push away from the cursor
                var dx = d.x - mouse.x, dy = d.y - mouse.y;
                var dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 120 && dist > 0) {
                    d.x += (dx / dist) * 1.4;
                    d.y += (dy / dist) * 1.4;
                }

                ctx.beginPath();
                ctx.fillStyle = 'rgba(226,178,92,' + d.a + ')';
                ctx.arc(d.x, d.y, d.r, 0, Math.PI * 2);
                ctx.fill();

                for (var j = i + 1; j < dots.length; j++) {
                    var o = dots[j];
                    var ex = d.x - o.x, ey = d.y - o.y;
                    var dd = Math.sqrt(ex * ex + ey * ey);
                    if (dd < 95) {
                        ctx.strokeStyle = 'rgba(126,162,255,' + ((1 - dd / 95) * 0.18) + ')';
                        ctx.lineWidth = 1;
                        ctx.beginPath();
                        ctx.moveTo(d.x, d.y);
                        ctx.lineTo(o.x, o.y);
                        ctx.stroke();
                    }
                }
            }
            raf = requestAnimationFrame(frame);
        }

        window.addEventListener('resize', resize);
        canvas.parentNode.addEventListener('mousemove', function (e) {
            var r = canvas.getBoundingClientRect();
            mouse.x = e.clientX - r.left;
            mouse.y = e.clientY - r.top;
        });

        resize();
        frame();

        // pause when the hero is off-screen
        if ('IntersectionObserver' in window) {
            new IntersectionObserver(function (entries) {
                running = entries[0].isIntersecting;
                if (running) { cancelAnimationFrame(raf); frame(); }
            }).observe(canvas);
        }
    }

    /* ---------- 5. Header, progress bar, scroll-spy, back to top ---------- */
    function scrollUI() {
        var header = $('#header');
        var bar = $('.progress span');
        var ticking = false;

        function update() {
            var y = window.pageYOffset;
            var max = document.documentElement.scrollHeight - window.innerHeight;
            header.classList.toggle('scrolled', y > 40);
            bar.style.transform = 'scaleX(' + (max > 0 ? y / max : 0) + ')';
            ticking = false;
        }
        window.addEventListener('scroll', function () {
            if (!ticking) { requestAnimationFrame(update); ticking = true; }
        }, { passive: true });
        update();

        // scroll-spy
        var links = $$('.nav a[href^="#"]');
        if ('IntersectionObserver' in window) {
            var spy = new IntersectionObserver(function (entries) {
                entries.forEach(function (en) {
                    if (en.isIntersecting) {
                        links.forEach(function (a) {
                            a.classList.toggle('active', a.getAttribute('href') === '#' + en.target.id);
                        });
                    }
                });
            }, { rootMargin: '-45% 0px -50% 0px' });
            ['home', 'features', 'desk'].forEach(function (id) {
                var el = document.getElementById(id);
                if (el) spy.observe(el);
            });
        }

        $('#toTop').addEventListener('click', function () { window.scrollTo({ top: 0, behavior: 'smooth' }); });
    }

    /* ---------- 6. Mobile menu ---------- */
    function mobileMenu() {
        var btn = $('#menuToggle');
        var nav = $('#nav');
        function set(open) {
            nav.classList.toggle('open', open);
            btn.setAttribute('aria-expanded', open);
            document.body.style.overflow = open ? 'hidden' : '';
        }
        btn.addEventListener('click', function () { set(!nav.classList.contains('open')); });
        $$('a', nav).forEach(function (a) { a.addEventListener('click', function () { set(false); }); });
        document.addEventListener('keydown', function (e) { if (e.key === 'Escape') set(false); });
    }

    /* ---------- 7. Reveal on scroll (staggered) ---------- */
    function reveals() {
        var items = $$('.reveal');

        // no IntersectionObserver? just show everything
        if (!('IntersectionObserver' in window)) {
            items.forEach(function (el) { el.classList.add('in', 'done'); });
            return;
        }

        // stagger siblings
        items.forEach(function (el) {
            var sibs = Array.prototype.slice.call(el.parentNode.children).filter(function (c) {
                return c.classList.contains('reveal');
            });
            el.style.setProperty('--d', (sibs.indexOf(el) * 0.12) + 's');
        });

        var io = new IntersectionObserver(function (entries) {
            entries.forEach(function (en) {
                if (!en.isIntersecting) return;
                var el = en.target;
                el.classList.add('in');
                if (el.classList.contains('card')) {
                    var delay = parseFloat(getComputedStyle(el).getPropertyValue('--d')) || 0;
                    setTimeout(function () { el.classList.add('done'); }, (delay + 1) * 1000);
                }
                io.unobserve(el);
            });
        }, { threshold: 0.05, rootMargin: '0px 0px -20px 0px' });

        items.forEach(function (el) { io.observe(el); });
    }

    /* ---------- 8. 3D tilt + spotlight on cards ---------- */
    function tiltCards() {
        if (!finePointer || reduceMotion) return;
        $$('.tilt').forEach(function (card) {
            card.addEventListener('mousemove', function (e) {
                var r = card.getBoundingClientRect();
                var px = (e.clientX - r.left) / r.width;
                var py = (e.clientY - r.top) / r.height;
                card.style.setProperty('--mx', (px * 100) + '%');
                card.style.setProperty('--my', (py * 100) + '%');
                if (card.classList.contains('done') || !card.classList.contains('reveal')) {
                    card.style.transform =
                        'perspective(900px) rotateX(' + ((0.5 - py) * 10).toFixed(2) + 'deg) rotateY(' + ((px - 0.5) * 12).toFixed(2) + 'deg) translateY(-6px)';
                }
            });
            card.addEventListener('mouseleave', function () { card.style.transform = ''; });
        });
    }

    /* ---------- 9. Magnetic buttons + ripple ---------- */
    function buttons() {
        if (finePointer && !reduceMotion) {
            $$('.magnetic').forEach(function (btn) {
                btn.addEventListener('mousemove', function (e) {
                    var r = btn.getBoundingClientRect();
                    var x = (e.clientX - r.left - r.width / 2) * 0.25;
                    var y = (e.clientY - r.top - r.height / 2) * 0.35;
                    btn.style.transform = 'translate(' + x + 'px, ' + (y - 3) + 'px)';
                });
                btn.addEventListener('mouseleave', function () { btn.style.transform = ''; });
            });
        }

        $$('.btn').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                var r = btn.getBoundingClientRect();
                var size = Math.max(r.width, r.height);
                var dot = document.createElement('span');
                dot.className = 'ripple';
                dot.style.width = dot.style.height = size + 'px';
                dot.style.left = (e.clientX - r.left - size / 2) + 'px';
                dot.style.top = (e.clientY - r.top - size / 2) + 'px';
                btn.appendChild(dot);
                setTimeout(function () { if (dot.parentNode) dot.parentNode.removeChild(dot); }, 700);
            });
        });
    }

    /* ---------- 10. Cursor glow ---------- */
    function cursorGlow() {
        var glow = $('.cursor-glow');
        if (!glow || !finePointer) return;
        var tx = 0, ty = 0, x = 0, y = 0;
        window.addEventListener('mousemove', function (e) {
            tx = e.clientX; ty = e.clientY;
            glow.classList.add('on');
        });
        document.addEventListener('mouseleave', function () { glow.classList.remove('on'); });
        (function loop() {
            x += (tx - x) * 0.12;
            y += (ty - y) * 0.12;
            glow.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
            requestAnimationFrame(loop);
        })();
    }

    /* ---------- 11. Toast ---------- */
    var toastTimer;
    function toast(msg) {
        var t = $('#toast');
        t.textContent = msg;
        t.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(function () { t.classList.remove('show'); }, 2800);
    }

    /* ---------- 12. Operation tabs (Add / Search / Issue / Return) ---------- */
    function tabs() {
        var bar = $('#tabs');
        var indicator = $('#tabIndicator');
        var buttonList = $$('.tab', bar);

        function activeButton() {
            return buttonList.filter(function (b) { return b.classList.contains('active'); })[0];
        }
        function moveIndicator(btn) {
            if (!btn) return;
            indicator.style.width = btn.offsetWidth + 'px';
            indicator.style.transform = 'translateX(' + btn.offsetLeft + 'px)';
        }
        function hasTab(id) {
            return buttonList.some(function (b) { return b.value === id; });
        }

        function open(id, opts) {
            var btn = buttonList.filter(function (b) { return b.value === id; })[0];
            if (!btn) return false;

            buttonList.forEach(function (b) {
                var on = b === btn;
                b.classList.toggle('active', on);
                b.setAttribute('aria-selected', on);
            });
            $$('.tab-panel').forEach(function (p) {
                var on = p.id === id;
                p.classList.toggle('active', on);
                p.hidden = !on;
            });
            moveIndicator(btn);

            if (opts && opts.scroll) {
                $('#panel').scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'center' });
                setTimeout(function () {
                    var first = $('input', $('#' + id));
                    if (first) first.focus({ preventScroll: true });
                }, 700);
            }
            return true;
        }

        buttonList.forEach(function (b) { b.addEventListener('click', function () { open(b.value); }); });

        // any link to #add-book / #search-book opens the right tab
        $$('a[href^="#"]').forEach(function (a) {
            var id = a.getAttribute('href').slice(1);
            if (hasTab(id)) {
                a.addEventListener('click', function (e) {
                    e.preventDefault();
                    open(id, { scroll: true });
                    history.replaceState(null, '', '#' + id);
                });
            }
        });

        // arrow-key navigation
        bar.addEventListener('keydown', function (e) {
            if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return;
            var i = buttonList.indexOf(activeButton());
            var step = e.key === 'ArrowRight' ? 1 : -1;
            var next = buttonList[(i + step + buttonList.length) % buttonList.length];
            open(next.value);
            next.focus();
        });

        window.addEventListener('resize', function () { moveIndicator(activeButton()); });

        // initial state (+ deep link support)
        var hash = location.hash.slice(1);
        var start = hasTab(hash) ? hash : 'add-book';
        requestAnimationFrame(function () {
            open(start);
            if (start === hash) setTimeout(function () { $('#panel').scrollIntoView({ block: 'center' }); }, 1200);
        });
        if (document.fonts && document.fonts.ready) {
            document.fonts.ready.then(function () { moveIndicator(activeButton()); });
        }
        window.addEventListener('load', function () { moveIndicator(activeButton()); });
    }

    /* ---------- 13. Form validation + loading state ---------- */
    function forms() {
        $$('.form-container').forEach(function (form) {
            var fields = $$('.form-group', form);

            function validate(group) {
                var input = $('input, select', group);
                var ok = input.checkValidity() && input.value.trim() !== '';
                group.classList.toggle('invalid', !ok);
                return ok;
            }

            fields.forEach(function (g) {
                var input = $('input, select', g);
                input.addEventListener('blur', function () { if (input.value !== '') validate(g); });
                input.addEventListener('input', function () { if (g.classList.contains('invalid')) validate(g); });
            });

            form.addEventListener('submit', function (e) {
                var results = fields.map(validate);
                if (results.indexOf(false) !== -1) {
                    e.preventDefault();
                    var bad = fields[results.indexOf(false)];
                    $('input, select', bad).focus();
                    toast('Please fix the highlighted fields.');
                    return;
                }
                var btn = $('button[type="submit"]', form);
                btn.classList.add('loading');
                $('.btn-label', btn).textContent = 'Working…';
                // disable AFTER the submit event so the form still posts normally
                setTimeout(function () { btn.disabled = true; }, 0);
            });
        });

        // re-enable buttons if the user navigates back (bfcache)
        window.addEventListener('pageshow', function (e) {
            if (!e.persisted) return;
            $$('button[type="submit"]').forEach(function (b) { b.disabled = false; b.classList.remove('loading'); });
        });
    }

    /* ---------- 14. Cover image preview and client-side checks ---------- */
    function coverUpload() {
        var input = $('#coverImage');
        var preview = $('#coverPreview');
        var image = $('#coverPreviewImage');
        var error = $('#coverError');
        var remove = $('#removeCover');
        if (!input || !preview || !image || !error || !remove) return;

        var currentUrl = null;
        var fileError = '';
        var allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
        var maxBytes = 5 * 1024 * 1024;

        function showError(message) {
            fileError = message;
            error.textContent = message;
            error.classList.toggle('show', Boolean(message));
        }

        function clearPreview() {
            if (currentUrl) URL.revokeObjectURL(currentUrl);
            currentUrl = null;
            image.removeAttribute('src');
            preview.hidden = true;
        }

        input.addEventListener('change', function () {
            showError('');
            clearPreview();
            var file = input.files && input.files[0];
            if (!file) return;

            if (allowedTypes.indexOf(file.type) === -1) {
                showError('Choose a JPG, PNG, or WebP image.');
                input.value = '';
                return;
            }
            if (file.size > maxBytes) {
                showError('The cover image must be 5 MB or smaller.');
                input.value = '';
                return;
            }

            currentUrl = URL.createObjectURL(file);
            image.src = currentUrl;
            preview.hidden = false;
        });

        remove.addEventListener('click', function () {
            input.value = '';
            showError('');
            clearPreview();
        });

        var form = input.closest('form');
        if (form) {
            form.addEventListener('submit', function (event) {
                if (!fileError) return;
                event.preventDefault();
                input.focus();
                toast(fileError);
            }, true);
        }
    }

    /* ---------- Collection page search, delete confirmation, and notices ---------- */
    function collectionInteractions() {
        var filter = $('#collectionFilter');
        var cards = $$('.book-card');
        var count = $('.collection-count');
        if (filter) {
            filter.addEventListener('input', function () {
                var query = filter.value.trim().toLocaleLowerCase();
                var visible = 0;
                cards.forEach(function (card) {
                    var text = ((card.getAttribute('data-title') || '') + ' ' + (card.getAttribute('data-author') || '')).toLocaleLowerCase();
                    var show = text.indexOf(query) !== -1;
                    card.hidden = !show;
                    if (show) visible++;
                });
                if (count) count.textContent = visible + (visible === 1 ? ' book' : ' books');
            });
        }

        $$('form[data-confirm-delete]').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!window.confirm('Delete this book from your collection?')) event.preventDefault();
            });
        });
        $$('form[data-confirm-cover-remove]').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!window.confirm('Remove this book cover?')) event.preventDefault();
            });
        });

        var notices = {
            added: 'Book added to your collection.',
            deleted: 'Book deleted.',
            'delete-failed': 'Could not delete that book.',
            issued: 'Book issued.',
            'issue-failed': 'No available copies to issue.',
            returned: 'Book returned.',
            'return-failed': 'Could not return that book.',
            updated: 'Book details updated.',
            'update-failed': 'Could not update that book.',
            'invalid-id': 'The book ID was not valid.',
            'invalid-input': 'Check the book details and try again.',
            'invalid-cover': 'Choose a valid JPG, PNG, or WebP cover under 5 MB.',
            'add-failed': 'The book could not be saved. Please try again.',
            'cover-updated': 'Cover saved to MySQL.',
            'cover-removed': 'Cover removed.',
            'cover-failed': 'Could not update the cover. Please try again.'
        };
        var notice = new URLSearchParams(window.location.search).get('notice');
        var added = new URLSearchParams(window.location.search).get('added');
        if (notice && notices[notice]) toast(notices[notice]);
        else if (added === '1') toast(notices.added);
    }

    /* ---------- 15. Click a spine -> go to the collection ---------- */
    function spines() {
        document.addEventListener('click', function (e) {
            var s = e.target.closest ? e.target.closest('.spine') : null;
            if (s) window.location.href = 'viewBooks';
        });
    }

    /* ---------- INIT ---------- */
    function init() {
        // each module runs separately so one failure can never break the rest
        [splitTitle, buildShelves, shelfParallax, particles, scrollUI, mobileMenu,
            reveals, tiltCards, buttons, cursorGlow, tabs, coverUpload, forms,
            collectionInteractions, spines].forEach(function (fn) {
            try { fn(); } catch (err) { if (window.console) console.error('LibraryHub: ' + fn.name + ' failed', err); }
        });

    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
